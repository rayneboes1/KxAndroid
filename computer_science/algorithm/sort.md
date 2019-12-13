# 排序

## 基本排序

三种时间复杂度为O\(n^2\)的排序算法。

### 冒泡排序

```text
  
public class BubbleSort implements ISort {


    @Override
    public void sort(int[] arr) {
        if (arr == null || arr.length <= 1) {
            return;
        }
        //这里边界只需要循环n-1次即可
        for (int i = 0; i < arr.length - 1; i++) {
            //减少不必要的循环次数
            boolean swapFlag = false;

            for (int j = 0; j < arr.length - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    Util.swap(arr, j, j + 1);
                    swapFlag = true;
                }
            }
            if (!swapFlag) {
                break;
            }

        }

    }
}
```

### 插入排序

```text
public class InsertSort implements ISort {


    @Override
    public void sort(int[] arr) {
        if (arr == null || arr.length <= 1) {
            return;
        }

        for (int i = 1; i < arr.length; i++) {
            int t = arr[i];

            int j;
            for (j = i - 1; j >= 0; j--) {
                //升序
                if (arr[j] > t) {
                    arr[j + 1] = arr[j];
                } else {
                    break;
                }
            }
            arr[j+1] = t;
        }
    }
}
```

### 选择排序

```text
public class SelectSort implements ISort {

    @Override
    public void sort(int[] arr) {
        if (arr == null || arr.length <= 1) {
            return;
        }

        for (int i = 0; i < arr.length - 1; i++) {
            int min = arr[i];
            int minIndex = i;
            for (int j = i + 1; j < arr.length; j++) {
                if (arr[j] < min) {
                    min = arr[j];
                    minIndex = j;
                }
            }
            if (i != minIndex) {
                Util.swap(arr, i, minIndex);
            }
        }
    }
}
```

## 高级排序

### 归并排序

```text
  public class MergeSort implements ISort {
    @Override
    public void sort(int[] arr) {
        mergeSortRecur(arr, 0, arr.length - 1);
    }

    /**
     * 递归实现的归并排序
     */
    private void mergeSortRecur(int[] arr, int start, int end) {
        if (start >= end) {
            return;
        }
        int middle = (start + end) / 2;
        mergeSortRecur(arr, start, middle);
        mergeSortRecur(arr, middle + 1, end);
        merge(arr, start, middle, end);

    }

    private void merge(int[] arr, int start, int middle, int end) {

        int i = start;
        int j = middle + 1;
        int k = 0;

        int[] tmp = new int[end - start + 1];

        while (i <= middle && j <= end) {
            if (arr[i] > arr[j]) {
                tmp[k++] = arr[j++];
            } else {
                tmp[k++] = arr[i++];
            }
        }

        //确定哪部分数组还有剩余
        int s = i <= middle ? i : j;

        while (k < tmp.length) {
            tmp[k++] = arr[s++];
        }

        for (int l = start; l <= end; l++) {
            arr[l] = tmp[l - start];
        }
    }
}
```

### 快速排序

```text
public class QuickSort implements ISort {
    @Override
    public void sort(int[] arr) {
        if (arr == null || arr.length <= 1) {
            return;
        }

        quickSortRecur(arr, 0, arr.length - 1);
    }

    private void quickSortRecur(int[] arr, int start, int end) {
        if (start >= end) {
            return;
        }
        int p = partition(arr, start, end);
        //注意边界是 p-1 和 p+1,因为中间元素的最终位置已确定
        quickSortRecur(arr, start, p-1);
        quickSortRecur(arr, p + 1, end);
    }

    private int partition(int[] arr, int s, int t) {
        int pivot = arr[t];

        int i = s;
        //注意边界 j<t,是把最后一个元素之前的元素与其进行比较
        for (int j = s; j < t; j++) {
            if (arr[j] < pivot) {
                Util.swap(arr, j, i);
                i++;
            }
        }
        Util.swap(arr, i, t);
        return i;
    }
}
```

### 希尔排序

### 堆排序

堆的结构可以查看[相关章节](../datastructure/dui.md)

## 特殊排序

### 桶排序

### 基数排序

### 计数排序

