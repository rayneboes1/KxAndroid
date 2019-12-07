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
