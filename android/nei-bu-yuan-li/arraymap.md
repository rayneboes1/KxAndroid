# ArrayMap

ArrayMap 是一个支持泛型的键值对映射数据结构，功能上类似于HashMap，但它比 HashMap 对内存的利用更有效。内部基于数组和二分查找实现，查找效率不及 HashMap，适用于少量元素的情况。

ArrayMap 位于 android.util 包下，实现了 Map 接口。为了更好的利用内存，ArrayMap 会随着元素的移动而缩减数组的长度，可能会导致当前的容量小于设置的容量。



## 属性

实例变量

```text
// 是否保证 HashCode 唯一
final boolean mIdentityHashCode;
// 保存 HashCode 的数组
int[] mHashes;
// 保存 key 和 value 的数组
Object[] mArray;
// ArrayMap 的映射对数量
int mSize;
```



```text
//静态

//是否在并发修改时抛出异常
private static final boolean CONCURRENT_MODIFICATION_EXCEPTIONS = true;
private static final int BASE_SIZE = 4;
//缓存的数组的最大数量
private static final int CACHE_SIZE = 10;

static final int[] EMPTY_IMMUTABLE_INTS = new int[0];
public static final ArrayMap EMPTY = new ArrayMap<>(-1);

//缓存 size 为 4 的数组
static Object[] mBaseCache;
static int mBaseCacheSize;
//缓存 size 为 4*2 的数组
static Object[] mTwiceBaseCache;
static int mTwiceBaseCacheSize;


```





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

ArrayMap 暴露了三个构造方法，如下面代码所示，最终都会调用到有两个参数的构造方法，但是该构造方式被 `@hide` 标记了，因此不能直接调用。通过构造函数的调用关系可以看到，`mIdentityHashCode` 始终是`false`的，也就是允许哈希冲突。

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

在构造方法中，处理对`capacity <0` 的情况进行特殊处理外，调用了 `allocArrays` 方法来创建数组并赋值给`mHashes`和 `mArray`。

 allocArrays 方法代码如下：

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
    //没有缓存的数组或者缓存的数组长度不满足条件
    mHashes = new int[size];
    //mArray 的容量是 size 的 2 倍
    mArray = new Object[size<<1];
}
```

allocArray 的前面一部分代码的逻辑是，如果要申请的数组长度是BASE\_SIZE或者BASE\_SIZE的2倍，那么优先利用缓存的数组，如果没有缓存数组或者申请的数组长度不符合这两种情况，在创建新数组。

由于 ArrayMap 通常都是存储少量数据，通过缓存数组可以避免频繁的创建数组，有效减少垃圾回收的影响。

关于缓存数组的逻辑后面再看。

可以看到 mArray 的容量是 mHashes 的 2 倍，这跟如何存储 key 和 value 有关，先看put方法。

## put

put 方法是Map 接口的，用于存入一个键值对。在 key 存在时会更新value的值并返回旧的value，当key不存在时就插入value 返回null。代码如下：

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
        //由前面构造方法可以看到，mIdentityHashCode 始终为false，因此 hash=key.hashCode();
        hash = mIdentityHashCode ? System.identityHashCode(key) : key.hashCode();
        index = indexOf(key, hash);
    }
    //已经存在与 key 对应的映射，直接更新value并返回旧的value
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

put 方法的主要逻辑为：**先根据 key 的hashCode 在 mHashes 数组中通过二分查找法查找是否存在，如果存在且mArray对应的位置页存在该 key，那么更新value并返回旧的value。否则，就执行插入，必要时进行数组扩容。**

这里面有很多细节，一一来看。

首先是对于 key 为 null 时的查找，调用了 indexOfNull 方法，代码如下：

### indexOfNull

```text
int indexOfNull() {
    final int N = mSize;

    // 没有数据，直接返回
    if (N == 0) {
        return ~0;
    }
    // 在 mHashes 数组的 0~N-1 范围内，使用二分查找法查找是否存在 0
    int index = binarySearchHashes(mHashes, N, 0);

    // 没有找到
    if (index < 0) {
        return index;
    }

    // mHashes 数组中存在 0，并且 mArray 对应的位置 key 也是 null，返回该下标
    if (null == mArray[index<<1]) {
        return index;
    }

    // mHashes 数组中存在 0，但 mArray 对应的位置 key 不是 null
    // 存在哈希冲突，继续向后查找
    int end;
    for (end = index + 1; end < N && mHashes[end] == 0; end++) {
        if (null == mArray[end << 1]) return end;
    }

    // mHashes 数组中存在 0，但 mArray 对应的位置 key 不是 null
    // 存在哈希冲突，继续向前查找
    for (int i = index - 1; i >= 0 && mHashes[i] == 0; i--) {
        if (null == mArray[i << 1]) return i;
    }

    // 没有找到，返回一个负值。同时把第一个hash不相等的下标返回
    // 以便下次插入时尽量少的移动元素
    return ~end;
}
```

主要的代码语句我都加了注释，可以看出 ArrayMap 使用了线性探测法处理哈希冲突，在hashCode 冲突但是key不匹配时，返回数组末尾的位置，可以减少插入元素时复制的元素数量。



对于不为 null 的 key，调用了 indexOf\(key,hash\) 来查找，该方法代码如下：

### indexOf

```text
int indexOf(Object key, int hash) {
    final int N = mSize;

    // 没有数据直接返回
    if (N == 0) {
        return ~0;
    }

    //使用二分查找法查找在 mHashes 数组中查找 hash
    int index = binarySearchHashes(mHashes, N, hash);

    // hash 没有找到，不存在映射对
    if (index < 0) {
        return index;
    }

    // hash 存在且 mArray 对应位置的 key 匹配 
    if (key.equals(mArray[index<<1])) {
        return index;
    }

    // hash 存在但是key 不匹配，继续向后搜索
    int end;
    for (end = index + 1; end < N && mHashes[end] == hash; end++) {
        if (key.equals(mArray[end << 1])) return end;
    }

    // hash 存在但是key 不匹配，继续向前搜索
    for (int i = index - 1; i >= 0 && mHashes[i] == hash; i--) {
        if (key.equals(mArray[i << 1])) return i;
    }

    // 没有找到符合的key，返回负数。同时把第一个不等于hash的下标返回
    // 以便下次插入时尽量少的移动元素
    return ~end;
}
```

indexOf 和 indexOfNull 的逻辑是一样的，不过在比较key时，是通过 equals 方法来进行的。

以上是针对 key 的查找逻辑。

当 index &gt;=0 时，也就是 ArrayMap 中已经存在相同 key 的映射，只需要更新值就可以了，put 方法中更新值的操作如下：

```text
if (index >= 0) {
    //index 是 key 的 hashCode 在 mHashes 中的下标 
    //在 mArray 数组中，键所在的下标为index*2,值所在的下标为index*2+1
    index = (index<<1) + 1;
    final V old = (V)mArray[index];
    mArray[index] = value;
    //返回旧值
    return old;
}
```

上面几行代码的重点是，对于 hashCode 在 mHashes 数组中的下标为index 的key，对应的value在 mArray 数组中的下标为 index\*2+1,而 key 在 mArray 中的下标为 index\*2，这一点从上面的搜索逻辑也可以看出来。

举例来说，对于一个key，如果它的hashCode 在 mHashes 中的下标为 1，那么这个 key 在mArray 中的下标为 1\*2=2，它对应的value在mArray 中的位置为 1\*2+1=3。

我们可以通过插入新值的逻辑再次验证一下，put 方法中插入新值的逻辑如下：

```text
//插入新值到 mHashes 和 mArray
mHashes[index] = hash;
// key 的下标为 index*2
mArray[index<<1] = key;
// value 的下标为 index*2+1
mArray[(index<<1)+1] = value;
//mSize + 1
mSize++;
```

这样我们就搞清楚了 ArrayMap 到底是怎么存储 hashCode、key和value的。

### indexOfXx 的返回值

对于 put 方法中，当key不存在时，有这么一句代码：

```text
index = ~index;
```

其中 index 为indexOf 方法的返回值，由于此时key并不存在，所有 index是个负数，那为什么要对其取反呢？简单的说，因为取反后的位置就是新的key 要插入的位置。

具体来说，分为两种情况，第一种是二分查找搜索直接返回负值的情况，这种情况在分析SparseArray 时已经说了，返回的是第一个大于要查找的值的下标，也就是它要插入的位置，具体分析可以查看[相关内容](sparsearray.md#put)，同时也解释了为什么 mHashes 是有序的。

第二种情况则是在 mHashes 中查找到了 key 的哈希值，但是没有在 mArray 中找到对应的 key，这种情况对应的代码如下：

```text
// hash 存在但是key 不匹配，继续向后搜索
int end;
for (end = index + 1; end < N && mHashes[end] == hash; end++) {
    if (key.equals(mArray[end << 1])) return end;
}

// hash 存在但是key 不匹配，继续向前搜索
for (int i = index - 1; i >= 0 && mHashes[i] == hash; i--) {
    if (key.equals(mArray[i << 1])) return i;
}

// 没有找到符合的key，返回负数。同时把第一个hash不相等的下标返回
// 以便下次插入时尽量少的移动元素
return ~end;
```

我们可以举个例子，假设 mHashes 中的元素为\[1,2,4,4,4,4,5,6\]，那么通过二分查找法，回先返回下标3，也就是第二个 4，如果 mArray 中没有对应的key，那么先向后搜索，假设直到最后一个4依然没有找到对应的key，那么此时end=6,也就是元素5的位置。

接下来向前搜索，假设也没有找到对应的key，那么此时就返回\(6=-7\)，即 put 中 index 为-7， 而当通过 index=~index 再次取反时，index=6，也就是hash为4的key应该插入的位置。当插入这个key时，只需要移动5，6两个元素就可以了，如果返回第一个4的位置，那么需要移动的元素就是6个，这就是为什么要返回最后一个hash值相等的下标。

现在应搞清楚了键的搜索、键值对的更新和插入逻辑，还有重要的逻辑没有说，就是数组扩容。

### 数组扩容

把数组扩容部分的代码单独再复制一遍：

```text
if (osize >= mHashes.length) {
    //数组空间不足，需要先扩容
    final int n = osize >= (BASE_SIZE*2) ? (osize+(osize>>1))
            : (osize >= BASE_SIZE ? (BASE_SIZE*2) : BASE_SIZE);

    //保存旧数组
    final int[] ohashes = mHashes;
    final Object[] oarray = mArray;
        
    //通过 allocArrays 创建（复用）数组并赋值给 mHashes 和 mArray
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
    //释放（缓存）数组空间
    freeArrays(ohashes, oarray, osize);
 }

if (index < osize) {
   //在数组中间插入，需要移动插入位置后面的元素
    System.arraycopy(mHashes, index, mHashes, index + 1, osize - index);
    System.arraycopy(mArray, index << 1, mArray, (index + 1) << 1, (mSize - index) << 1);
}
```

扩容策略为如果当前大小大于 BASE\_SIZE\*2=4\*2=8，那么扩容为原来的1.5倍，如果当前大小小于 8 但是大于4，那么扩容后数组大小为8；如果当前大小小于4，那么扩容为 4。

扩容后的容量大小确定后，通过 allocArray 方法创建数组并赋值给 mHashes 和 mArray。

前面在分析构造方法时，已经看到过这个方法，这里仔细分析下，该方法代码如下：

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
    //没有缓存的数组或者缓存的数组长度不满足条件
    mHashes = new int[size];
    //mArray 的容量是 size 的 2 倍
    mArray = new Object[size<<1];
}
```

方法中，针对容量为BASE\_SIZE或者BASE\_SIZE \*2 的情况，优先利用已经缓存的数组，我们以容量为BASE\_SIZE时的情况分析，代码逻辑是：

1. 将 mBaseCache 赋值给 mArray
2. 将 mBaseCache 赋值为 mArray\[0\]
3. 将 mHashes 赋值为 mArray\[1\]
4. 将 mArray\[0\] mArray\[1\]的值置空
5. 缓存数量减一

要想弄清楚这段逻辑，就要知道 mBaseCache 究竟存储的究竟是什么，先来看看注释：

```text
/**
* Caches of small array objects to avoid spamming garbage.  The cache
* Object[] variable is a pointer to a linked list of array objects.
* The first entry in the array is a pointer to the next array in the
* list; the second entry is a pointer to the int[] hash code array for it.
*/
static Object[] mBaseCache;
```

说实话，这段注释我看了好几遍依然有点懵。大概意思是 mBaseCache 是指向链表的指针，而这个链表是由\(所有被缓存的\)数组组成的。其中 mBaseCache\[0\] 指向下一个被缓存的数组，mBaseCache\[1\] 指向的当前数组对应的 mHashes 数组。

所以 mBaseCache 里的内容大概如下图所示：

（画图）

对照着图在看上面的逻辑就很容易理解了，mTwiceBaseCache 与 mBaseCache 除了缓存的数组长度不一致以外，其他都是相同的。

那么 mBaseCache 和 mTwiceBaseCache 是什么时候被赋值的呢？一定是回收数组时。在put方法中，在分配完新数组并从旧数组拷贝完元素之后，调用了 freeArrays 方法释放旧的数组，该方法代码如下：

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

可以看到只针对数组长度为 BASE\_SIZE 和 BASE\_SIZE \*2 的情况做了缓存。还是只看 BASE\_SIZE 的情况，将 array\[1\]指向了对应的 hashes 数组，然后将 array\[0\] 指向 mBaseCache,在把 mBaseCache 指向 array,形成了链表。同时把 array 中 下标大于1的元素置空，否则会存在内存泄漏。

以上就是数组被缓存和复用的逻辑，这也是我认为 ArrayMap 最难理解的地方。

## get



