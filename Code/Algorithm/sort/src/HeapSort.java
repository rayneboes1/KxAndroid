public class HeapSort implements ISort {
    @Override
    public void sort(int[] arr) {
        //假设数组下标0没有存储数据
        int n = arr.length - 1;
        //1. 建大顶(升序)堆
        buildHeap(arr, n);
        //2. 每次把堆顶元素与结尾交换
        for (int i = 0; i < n; i++) {
            Util.swap(arr, 1, n - i);
            heapify(arr, n - 1 - i, 1);
        }
    }

    private void buildHeap(int[] arr, int n) {
        //从第一个非叶子节点开始向下调整
        for (int i = n / 2; i >= 1; i--) {
            heapify(arr, n, i);
        }

    }

    /**
     * 自顶向下堆化
     */
    private void heapify(int[] arr, int n, int i) {
        while (true) {
            int maxPos = i;
            if (i * 2 <= n && arr[i * 2] > arr[maxPos]) {
                maxPos = i * 2;
            }
            if (i * 2 + 1 <= n && arr[i * 2 + 1] > arr[maxPos]) {
                maxPos = i * 2 + 1;
            }
            if (maxPos == i) {
                break;
            }
            Util.swap(arr, i, maxPos);
            //继续向下调整
            i = maxPos;
        }
    }
}
