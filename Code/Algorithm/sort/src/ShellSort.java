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
