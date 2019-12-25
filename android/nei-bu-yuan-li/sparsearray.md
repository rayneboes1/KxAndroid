# SparseArray

## 简介

SparseArray 建立整数和对象之间的映射，比HashMap&lt;Integer,Object&gt;高效。一是因为避免了自动装箱过程，而是不依赖一个额外的Entry对象。

寻找key时使用了二分查找，对于大量的数据不是很高效。

为了提升性能，当移除元素时，并不会删除元素并移动数组，而是将要删除的元素标记为删除，可以实现复用；当「垃圾回收」时才回将标记为删除的元素真正移除。当数组需要扩容或者获取数组size时，「垃圾回收」才会进行。

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



libcore/luni/src/main/java/libcore/util/EmptyArray.java

```text
public static final int[] INT = new int[0];
public static final Object[] OBJECT = new Object[0];
```

## put

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

ContainerHelpers\#binarySearch

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

未找到时，lo为最后一个大于key的位置，因此直接插入即可，省去了排序的过程。



```text
public static boolean[] insert(boolean[] array, int currentSize, int index, boolean element) {
        assert currentSize <= array.length;

        if (currentSize + 1 <= array.length) {
            System.arraycopy(array, index, array, index + 1, currentSize - index);
            array[index] = element;
            return array;
        }

        boolean[] newArray = ArrayUtils.newUnpaddedBooleanArray(growSize(currentSize));
        System.arraycopy(array, 0, newArray, 0, index);
        newArray[index] = element;
        System.arraycopy(array, index, newArray, index + 1, array.length - index);
        return newArray;
    }
```



```text
public static int growSize(int currentSize) {
        return currentSize <= 4 ? 8 : currentSize * 2;
    }
```

## get

```text
public E get(int key, E valueIfKeyNotFound) {
    int i = ContainerHelpers.binarySearch(mKeys, mSize, key);

    if (i < 0 || mValues[i] == DELETED) {
        return valueIfKeyNotFound;
    } else {
        return (E) mValues[i];
    }
}
```





## size

## gc

## clone

//数组调用clone拷贝

