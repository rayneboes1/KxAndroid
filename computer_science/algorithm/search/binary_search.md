# 二分查找

### 无重复元素查找

```text
/**
 * 二分查找
 * 
 * @param arr   无重复元素的 有序数组
 * @param value 待查找目标值
 * @return 如果数组中存在值相等的元素，返回该元素下标否则返回-1
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

### 第一个大于等于

### 最后一个小于等于



