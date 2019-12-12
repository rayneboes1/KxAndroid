---
description: 二分查找及其变体问题
---

# 二分查找

### 无重复元素查找

```text
/**
 * 二分查找
 * 
 * @param arr   无重复元素的有序数组(升序)
 * @param value 待查找目标值
 * @return 符合条件的元素下标，否则返回-1
 */
public static int binarySearch(int[] arr, int value) {
    if (arr == null || arr.length == 0) {
        return -1;
    }
    int start = 0;
    int end = arr.length - 1;
    while (start <= end) {
        // 如果使用 (start+end)/2 在start 和 end 较大时有溢出风险
        int middle = start + ((end - start) >> 1);
        if (arr[middle] == value) {
            return middle;
        } else if (arr[middle] > value) {
            end = middle - 1;
        } else {
            start = middle + 1;
        }
    }
    return -1;
}

```

### 寻找第一个等于指定值的元素

```text
/**
 * 寻找第一个等于给定值的元素
 * 
 * @param arr 有重复元素的有序数组（升序）
 * @param value 待查找目标值
 * @return 符合条件的元素下标，否则返回-1
 */
public static int binarySearchFirst(int[] arr, int value) {
    if (arr == null || arr.length == 0) {
        return -1;
    }
    int start = 0;
    int end = arr.length - 1;
    while (start <= end) {
        int middle = start + ((end - start) >> 1);
        if (arr[middle] > value) {
            end = middle - 1;
        } else if (arr[middle] < value) {
            start = middle + 1;
        } else {
            if (middle == 0 || arr[middle - 1] != value) {
                return middle;
            }
            end = middle - 1;
        }
    }
    return -1;
}
```

### 寻找最后一个等于指定值的元素

```text
/**
 * 寻找最后一个等于指定值的元素
 * @param arr 有重复元素的有序数组
 * @param value 待查找目标值
 * @return 符合条件的元素下标，否则返回-1
 */
public static int binarySearchLast(int[] arr, int value) {
    if (arr == null || arr.length == 0) {
        return -1;
    }
    int start  = 0;
    int end = arr.length - 1;
    while (start <= end) {
        int middle = start + ((end - start) >> 1);
        if (arr[middle] > value) {
            end = middle - 1;
        } else if (arr[middle] < value) {
            start = middle + 1;
        } else {
            if (middle == arr.length - 1 || arr[middle + 1] != value) {
                return middle;
            }
            start = middle + 1;
        }
    }
    return -1;
}
```

### 第一个大于等于指定值的元素

针对升序数组。

```text
/**
 * 寻找第一个大于等于指定值的元素
 * 
 * @param arr   有序数组
 * @param value 指定值
 * @return 符合条件的元素下标，否则返回-1
 */
public static int binarySeartchFirstNotLess(int[] arr, int value) {
    if (arr == null || arr.length == 0) {
        return -1;
    }
    int start = 0;
    int end = arr.length - 1;
    while (start <= end) {
        int middle = start + ((end - start) >> 1);
        if (arr[middle] >= value) {
            if (middle == 0 || arr[middle - 1] < value) {
                return middle;
            }
            end = middle - 1;
        } else {
            start = middle + 1;
        }
    }
    return -1;
}
```

### 最后一个小于等于指定值的元素

针对升序数组。

```text
/**
 * 寻找最后一个小于等于指定值的元素
 * @param arr 有序数组（升序）
 * @param value 指定值
 * @return 符合条件的元素下标，否则返回-1
 */
public static int binarySearchLastNotLarger(int[] arr, int value) {
    if (arr == null || arr.length == 0) {
        return -1;
    }
    int start = 0;
    int end = arr.length - 1;
    while (start <= end) {
        int middle = start + ((end - start) >> 1);
        if (arr[middle] <= value) {
            if (middle == arr.length - 1 || arr[middle + 1] > value) {
                return middle;
            }
            start = middle + 1;
        } else {
            end = middle - 1;
        }
    }
    return -1;
}
```

