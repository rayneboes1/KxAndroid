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
                //升序>
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
