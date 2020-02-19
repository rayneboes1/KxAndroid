---
description: 时间复杂度为O(n^2)的排序算法
---

# 简单排序

## 冒泡排序

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

## 插入排序

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
            //此时 arr[j] 为第一个小于 t 的位置，或者为-1
            arr[j+1] = t;
        }
    }
}
```

## 选择排序

以第一个元素为初始值，在后面的所有元素中找到最小值，如果这个值比初始值还小，那么就交互两个元素位置。依次类推，直到最后。

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

## 

