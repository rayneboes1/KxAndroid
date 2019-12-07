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
