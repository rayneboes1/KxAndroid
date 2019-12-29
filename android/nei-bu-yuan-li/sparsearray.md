# SparseArray

## 简介

SparseArray（稀疏数组） 用于建立整数和对象之间的映射，比 HashMap&lt;Integer,Object&gt; 更高效。一是因为避免了自动装箱过程，二是不依赖一个额外的 Entry 包装对象。内部采用了两个数组来存储key和value。寻找key时使用了二分查找，对于大量的数据就不是很高效。

为了提升性能，当移除元素时，并不会在数组中删除元素并移动数组中的其他元素，而是将要删除的元素标记为已删除；当「垃圾回收」时才会将标记为删除的元素真正移除。当数组需要扩容或者获取数组size时，「垃圾回收」才会进行。

[SparseArray 源码](https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/android/util/SparseArray.java;l=56;bpv=1;bpt=1?q=SparseArray)

## 属性

```text
public class SparseArray<E> implements Cloneable {
    //所有被删除的元素都会先指向这个对象
    private static final Object DELETED = new Object();
    //是否需要进行空间回收
    private boolean mGarbage = false;
    //存放key的数组
    private int[] mKeys;
    //存放value的数组
    private Object[] mValues;
    //当前元素数量
    private int mSize;
    
    //.. 
}
```

SparseArray 采用了两个数组分别存储 key 和 value，其中存储key的数组为`int[]`类型。

## 构造方法

```text
public SparseArray(int initialCapacity) {
    if (initialCapacity == 0) {
        mKeys = EmptyArray.INT;
        mValues = EmptyArray.OBJECT;
    } else {
        mValues = ArrayUtils.newUnpaddedObjectArray(initialCapacity);
        mKeys = new int[mValues.length];
    }
    mSize = 0;
}

public SparseArray() {
    //默认容量是10
    this(10);
}
```

如果不指定容量，则SparseArray 默认大小是10。当初始容量为0时，代码将mkeys和mValues初始化为 EmptyArray 的常量，避免重复创建对象。

```text
public static final int[] INT = new int[0];
public static final Object[] OBJECT = new Object[0];
```

[EmptyArray 源码](https://cs.android.com/android/platform/superproject/+/master:libcore/luni/src/main/java/libcore/util/EmptyArray.java;bpv=1;bpt=1;l=24?q=EmptyArray)

## put

put 用于放入一个 int 到 E 类型对象的映射。put 方法源码如下：

```text
public void put(int key, E value) {
    int i = ContainerHelpers.binarySearch(mKeys, mSize, key);
    //给key已经存在元素，直接替换
    if (i >= 0) {
        mValues[i] = value;
    } else {
        i = ~i;

        if (i < mSize && mValues[i] == DELETED) {
            mKeys[i] = key;
            mValues[i] = value;
            return;
        }

        if (mGarbage && mSize >= mKeys.length) {
            gc();

            // Search again because indices may have changed.
            i = ~ContainerHelpers.binarySearch(mKeys, mSize, key);
        }

        mKeys = GrowingArrayUtils.insert(mKeys, mSize, i, key);
        mValues = GrowingArrayUtils.insert(mValues, mSize, i, value);
        mSize++;
    }
}
```

主要逻辑：

1. 查看key是否已经存在，如果存在，则直接更新对应value
2. 检查即将插入的位置value是否标记为已删除，如果是，直接在该位置更新key和value
3. 如果有必要，进行一次「垃圾回收」，并更新即将插入的位置（垃圾回收过程数组元素发生了移动，所以插入位置可能会变动）
4. 将key和value插入对应的位置。

这里有一个巧妙的设计，在进行二分查找时，如果没有找到对应元素，返回的是第一个大于key的元素下标按位取反的值。具体源码在ContainerHelpers 的 binarySearch 方法中：

```text
static int binarySearch(int[] array, int size, int value) {
        int lo = 0;
        int hi = size - 1;

        while (lo <= hi) {
            final int mid = (lo + hi) >>> 1;
            final int midVal = array[mid];

            if (midVal < value) {
                lo = mid + 1;
            } else if (midVal > value) {
                hi = mid - 1;
            } else {
                return mid;  // value found
            }
        }
        return ~lo;  // value not present
    }
```

在上面代码中，未找到时，lo为最后一个大于key的位置。**因此在put方法中，可以再次按位取反得到这个位置下标，然后执行更新或插入即可，省去了插入过程中再排序的过程。**

insert 方法逻辑比较简单，就是在数组的制定位置插入元素。

```text
public static boolean[] insert(boolean[] array, int currentSize, int index, boolean element) {
        assert currentSize <= array.length;
        //如果数组空间足够，直接把index后面的元素后移，然后更新index位置的值
        if (currentSize + 1 <= array.length) {
            System.arraycopy(array, index, array, index + 1, currentSize - index);
            array[index] = element;
            return array;
        }
        //数组空间不足时，先申请一个新的数组（大小为原数组的2倍）
        boolean[] newArray = ArrayUtils.newUnpaddedBooleanArray(growSize(currentSize));
        //复制 index 之前的元素
        System.arraycopy(array, 0, newArray, 0, index);
        //新元素插入到 index 位置
        newArray[index] = element;
        复制原数组index之后的原素
        System.arraycopy(array, index, newArray, index + 1, array.length - index);
        return newArray;
    }
```

当数组空间不足时，则对其进行扩容，扩容后的大小使用 growSize 方法计算，源码如下，可以看到扩容为数组当前容量的二倍。

```text
public static int growSize(int currentSize) {
    return currentSize <= 4 ? 8 : currentSize * 2;
}
```

## get



```text
public E get(int key) {
    return get(key, null);
}
```



```text
public E get(int key, E valueIfKeyNotFound) {
    int i = ContainerHelpers.binarySearch(mKeys, mSize, key);
    //如果没有找到key或者key对应的元素被标记为删除
    if (i < 0 || mValues[i] == DELETED) {
        return valueIfKeyNotFound;
    } else {
        return (E) mValues[i];
    }
}
```



## remove



```text
public void remove(int key) {
    delete(key);
}
```



```text
public void delete(int key) {
    int i = ContainerHelpers.binarySearch(mKeys, mSize, key);

    if (i >= 0) {
        if (mValues[i] != DELETED) {
            mValues[i] = DELETED;
            mGarbage = true;
        }
    }
}
```





## size



```text
public int size() {
    if (mGarbage) {
        gc();
    }
    return mSize;
}
```

## gc

```text
private void gc() {
    int n = mSize;
    int o = 0;
    int[] keys = mKeys;
    Object[] values = mValues;
    for (int i = 0; i < n; i++) {
        Object val = values[i];
        if (val != DELETED) {
            if (i != o) {
                keys[o] = keys[i];
                values[o] = val;
                values[i] = null;
            }
            o++;
        }
    }
    mGarbage = false;
    mSize = o;
}
```

算法题 [26. 删除排序数组中的重复项](https://leetcode-cn.com/problems/remove-duplicates-from-sorted-array/) 



## clone

```text
public SparseArray<E> clone() {
    SparseArray<E> clone = null;
    try {
        clone = (SparseArray<E>) super.clone();
        clone.mKeys = mKeys.clone();
        clone.mValues = mValues.clone();
    } catch (CloneNotSupportedException cnse) {
        /* ignore */
    }
    return clone;
}
```

### 数组调用clone拷贝是深拷贝还是浅拷贝？

浅拷贝

