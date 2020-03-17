---
description: 时间复杂度为O(n^2)的排序算法
---

# 高级排序

## 归并排序

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

## 快速排序

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

## 希尔排序

```text
public class ShellSort implements ISort {
    @Override
    public void sort(int[] arr) {
        if (arr == null || arr.length == 0) {
            return;
        }

        int gap = arr.length / 2;
        while (gap > 0) {
            for (int i = gap; i < arr.length; i++) {
                //可以加一层判断减少执行时间
                int j = i - gap;
                int value = arr[i];
                for (; j >= 0 && arr[j] > value; j -= gap) {
                    Util.swap(arr, j, j + gap);
                }
                arr[j + gap] = value;
            }
            gap /= 2;
        }

    }
}
```

## 堆排序

堆的结构可以查看[相关章节](../../datastructure/heap/)。

堆排序过程主要分为两个步骤：**建堆和排序** 。

#### 建堆

建堆的过程就是把原数据组织成一个堆。有两种思路：

* 从第二个节点开始 ，按照插入的方式放入堆，然后自下向上进行调整
* 从最后一个非叶子节点开始，使用自顶向下调整的方式，直到根节点

```text

private static void buildHeap(int[] a, int n) {
  //从最后一个非叶子节点开始，使用自顶向下调整的方式，直到根节点
  for (int i = n/2; i >= 1; --i) {
    heapify(a, n, i);
  }
}

private static void heapify(int[] a, int n, int i) {
  while (true) {
    int maxPos = i;
    if (i*2 <= n && a[i] < a[i*2]) maxPos = i*2;
    if (i*2+1 <= n && a[maxPos] < a[i*2+1]) maxPos = i*2+1;
    if (maxPos == i) break;
    swap(a, i, maxPos);
    i = maxPos;
  }
}
```

建堆时间复杂度为O\(n\),推导过程见[专栏内容](https://time.geekbang.org/column/article/69913)。 

#### 排序

每次取堆顶元素与堆最后一个元素进行交换，然后将剩下的元素重新调整为堆，直到只剩一个元素，排序完成。

```text
// n表示数据的个数，数组a中的数据从下标1到n的位置。
public static void sort(int[] a, int n) {
  buildHeap(a, n);
  int k = n;
  while (k > 1) {
    swap(a, 1, k);
    --k;
    heapify(a, k, 1);
  }
}
```

堆排序的时间复杂度为O\(nlogn\)，不稳定。

#### 与快速排序相比

* 数据访问是跳跃性的，不能充分利用CPU缓存提升性能
* 交换次数要多于快速排序（有序的数据堆化后反而无序了）

![&#x6709;&#x5E8F;&#x6570;&#x636E;&#x5806;&#x5316;&#x540E;&#x53D8;&#x5F97;&#x66F4;&#x52A0;&#x65E0;&#x5E8F;](../../../.gitbook/assets/image%20%2815%29.png)

## 

