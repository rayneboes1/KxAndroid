import java.util.Arrays;

/**
 * 输出集合的全排列(未去重和优化)
 * https://zh.wikipedia.org/wiki/%E5%85%A8%E6%8E%92%E5%88%97%E7%94%9F%E6%88%90%E7%AE%97%E6%B3%95
 */
public class Permutation {


    public static void main(String[] args) {
        int[] arr = {1, 2, 3, 4};
        internal(arr, 0);
    }


    public static void resolution(int[] arr) {
        internal(arr, 0);
    }

    /**
     * 时间复杂度为n！
     */
    private static String internal(int[] arr, int start) {
        if (start == arr.length - 1) {
            System.out.println(Arrays.toString(arr));
            return Arrays.toString(arr);
        }
        String result = "";
        for (int i = start; i < arr.length; i++) {
            swap(arr, i, start);
            result = internal(arr, start + 1);
            //排序后需要换回来
            swap(arr, i, start);
        }
        return result;
    }

    private static void swap(int[] arr, int i, int j) {
        int t = arr[i];
        arr[i] = arr[j];
        arr[j] = t;
    }
}
