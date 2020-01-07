# ArrayMap

ArrayMap 是一个支持泛型的键值对映射数据结构，功能上类似于HashMap，但它比 HashMap 对内存的利用更有效。内部基于数组和二分查找实现，查找效率不及 HashMap，适用于少量元素的情况。

ArrayMap 位于 android.util 包下，实现了 Map 接口。为了更好的利用内存，ArrayMap 会随着元素的移动而缩减数组的长度，可能会导致当前的容量小于设置的容量。



二分查找

```text
private static int binarySearchHashes(int[] hashes, int N, int hash) {
    try {
        return ContainerHelpers.binarySearch(hashes, N, hash);
    } catch (ArrayIndexOutOfBoundsException e) {
        //CONCURRENT_MODIFICATION_EXCEPTIONS=true
        if (CONCURRENT_MODIFICATION_EXCEPTIONS) {
            throw new ConcurrentModificationException();
        } else {
            throw e; // the cache is poisoned at this point, there's not much we can do
        }
    }
}
```

## 构造方法

ArrayMap 暴露了三个构造方法，如下面代码所示，最终都会盗用至两个参数的构造方法，但是该构造方式被 `@hide` 标记了，因此不能直接调用。通过构造函数的调用关系可以看到，`mIdentityHashCode` 始终是`false`的。

```text
public ArrayMap(ArrayMap<K, V> map) {
    //调用无参构造方法
    this();
    if (map != null) {
        putAll(map);
    }
}

public ArrayMap() {
    this(0, false);
}

public ArrayMap(int capacity) {
    this(capacity, false);
}

/** {@hide} */
public ArrayMap(int capacity, boolean identityHashCode) {
    mIdentityHashCode = identityHashCode;

    // If this is immutable, use the sentinal EMPTY_IMMUTABLE_INTS
    // instance instead of the usual EmptyArray.INT. The reference
    // is checked later to see if the array is allowed to grow.
    if (capacity < 0) {
        mHashes = EMPTY_IMMUTABLE_INTS;
        mArray = EmptyArray.OBJECT;
    } else if (capacity == 0) {
        mHashes = EmptyArray.INT;
        mArray = EmptyArray.OBJECT;
    } else {
        allocArrays(capacity);
    }
    mSize = 0;
}
```

通常都是存储少量数据，缓存数组可以避免频繁的创建和回收数组。

通过 allocArrays 方法创建数组：

```text
private void allocArrays(final int size) {
    if (mHashes == EMPTY_IMMUTABLE_INTS) {
        throw new UnsupportedOperationException("ArrayMap is immutable");
    }
    //优先利用缓存的数组
    if (size == (BASE_SIZE*2)) {
        synchronized (ArrayMap.class) {
            if (mTwiceBaseCache != null) {
                final Object[] array = mTwiceBaseCache;
                mArray = array;
                mTwiceBaseCache = (Object[])array[0];
                mHashes = (int[])array[1];
                //将前两个元素置为空
                array[0] = array[1] = null;
                mTwiceBaseCacheSize--;
                return;
            }
        }
    } else if (size == BASE_SIZE) {
        synchronized (ArrayMap.class) {
            if (mBaseCache != null) {
                final Object[] array = mBaseCache;
                mArray = array;
                mBaseCache = (Object[])array[0];
                mHashes = (int[])array[1];
                array[0] = array[1] = null;
                mBaseCacheSize--;
                return;
            }
        }
    }
    mHashes = new int[size];
    //mArray 的容量是 size 的 2 倍
    mArray = new Object[size<<1];
}
```

## put

put 方法在 key 存在时会返回旧的value，当key不存在是返回null。

```text
public V put(K key, V value) {
    final int osize = mSize;
    final int hash;
    int index;
    // 在mHashes数组中查找对应的 hash 值的下标
    if (key == null) {
        hash = 0;
        index = indexOfNull();
    } else {
        //mIdentityHashCode 始终为false，因此 hash=key.hashCode();
        hash = mIdentityHashCode ? System.identityHashCode(key) : key.hashCode();
        index = indexOf(key, hash);
    }
    //已经存在与 key 对应的映射，直接更新value并返回
    if (index >= 0) {
        //index 为 key 的 hash 在 mHashes 数组中的下标， 
        //在 mArray 数组中，键所在的下标为index*2,值所在的下标为index*2+1
        index = (index<<1) + 1;
        final V old = (V)mArray[index];
        mArray[index] = value;
        //返回旧值
        return old;
    }
    //没有找到对应的key，执行插入
    //index 为应该插入的位置
    index = ~index;
    if (osize >= mHashes.length) {
        //数组空间不足，需要先扩容
        //扩容策略为 如果当前大小大于 BASE_SIZE*2=4*2=8,那么扩容为原来的1.5倍
        //如果 当前大小小于8但是大于4，那么扩容后数组大小为8；
        //如果当前大小小于4，那么扩容为 4
        final int n = osize >= (BASE_SIZE*2) ? (osize+(osize>>1))
                : (osize >= BASE_SIZE ? (BASE_SIZE*2) : BASE_SIZE);

        final int[] ohashes = mHashes;
        final Object[] oarray = mArray;
        
        //通过 allocArrays 创建数组并赋值给 mHashes 和 mArray
        allocArrays(n);

        if (CONCURRENT_MODIFICATION_EXCEPTIONS && osize != mSize) {
            //存在并发修改，抛出异常
            throw new ConcurrentModificationException();
        }

        if (mHashes.length > 0) {
            //将值从旧数组拷贝到新数组
            System.arraycopy(ohashes, 0, mHashes, 0, ohashes.length);
            System.arraycopy(oarray, 0, mArray, 0, oarray.length);
        }
        //释放数组空间
        freeArrays(ohashes, oarray, osize);
     }

    if (index < osize) {
       //在数组中间插入，需要移动插入位置后面的元素
        System.arraycopy(mHashes, index, mHashes, index + 1, osize - index);
        System.arraycopy(mArray, index << 1, mArray, (index + 1) << 1, (mSize - index) << 1);
    }

    if (CONCURRENT_MODIFICATION_EXCEPTIONS) {
        if (osize != mSize || index >= mHashes.length) {
            //存在并发修改，抛出异常
            throw new ConcurrentModificationException();
        }
    }
    //插入新值到 mHashes 和 mArray
    mHashes[index] = hash;
    mArray[index<<1] = key;
    mArray[(index<<1)+1] = value;
    //mSize + 1
    mSize++;
    return null;
}
```



indexOfNull

```text
int indexOfNull() {
    final int N = mSize;

    // Important fast case: if nothing is in here, nothing to look for.
    if (N == 0) {
        return ~0;
    }

    int index = binarySearchHashes(mHashes, N, 0);

    // If the hash code wasn't found, then we have no entry for this key.
    if (index < 0) {
        return index;
    }

    // If the key at the returned index matches, that's what we want.
    if (null == mArray[index<<1]) {
        return index;
    }

    // Search for a matching key after the index.
    int end;
    for (end = index + 1; end < N && mHashes[end] == 0; end++) {
        if (null == mArray[end << 1]) return end;
    }

    // Search for a matching key before the index.
    for (int i = index - 1; i >= 0 && mHashes[i] == 0; i--) {
        if (null == mArray[i << 1]) return i;
    }

    // Key not found -- return negative value indicating where a
    // new entry for this key should go.  We use the end of the
    // hash chain to reduce the number of array entries that will
    // need to be copied when inserting.
    return ~end;
}
```

indexOf

使用了线性探测法处理哈希冲突，返回数组末尾的位置，可以减少插入元素时复制的元素数量。

```text
int indexOf(Object key, int hash) {
    final int N = mSize;

    // Important fast case: if nothing is in here, nothing to look for.
    if (N == 0) {
        return ~0;
    }

    int index = binarySearchHashes(mHashes, N, hash);

    // If the hash code wasn't found, then we have no entry for this key.
    if (index < 0) {
        return index;
    }

    // If the key at the returned index matches, that's what we want.
    if (key.equals(mArray[index<<1])) {
        return index;
    }

    // Search for a matching key after the index.
    int end;
    for (end = index + 1; end < N && mHashes[end] == hash; end++) {
        if (key.equals(mArray[end << 1])) return end;
    }

    // Search for a matching key before the index.
    for (int i = index - 1; i >= 0 && mHashes[i] == hash; i--) {
        if (key.equals(mArray[i << 1])) return i;
    }

    // Key not found -- return negative value indicating where a
    // new entry for this key should go.  We use the end of the
    // hash chain to reduce the number of array entries that will
    // need to be copied when inserting.
    return ~end;
}
```



### freeArrays

```text
private static void freeArrays(final int[] hashes, final Object[] array, final int size) {
    if (hashes.length == (BASE_SIZE*2)) {
        synchronized (ArrayMap.class) {
            if (mTwiceBaseCacheSize < CACHE_SIZE) {
                //组成链表结构
                array[0] = mTwiceBaseCache;
                array[1] = hashes;
                for (int i=(size<<1)-1; i>=2; i--) {
                    array[i] = null;
                }
                mTwiceBaseCache = array;
                mTwiceBaseCacheSize++;
            }
        }
    } else if (hashes.length == BASE_SIZE) {
        synchronized (ArrayMap.class) {
            if (mBaseCacheSize < CACHE_SIZE) {
                array[0] = mBaseCache;
                array[1] = hashes;
                for (int i=(size<<1)-1; i>=2; i--) {
                    array[i] = null;
                }
                mBaseCache = array;
                mBaseCacheSize++;
            }
        }
    }
}
```





