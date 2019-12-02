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
