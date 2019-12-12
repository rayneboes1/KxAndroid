/**
 * 二分查找（针对有序数组）
 */
class BinarySearch {

    public static void main(String[] args) {

        int[] test1 = { 1, 2, 3, 4, 5, 8, 9 };
        int index = binarySearch(test1, 9);
        System.out.println("index=" + index);
        int indexNotFound = binarySearch(test1, 10);
        System.out.println("index=" + indexNotFound);

    }

    /**
     * 二分查找
     * 
     * @param arr   无重复元素的 有序数组
     * @param value 待查找目标值
     * @return 如果数组中存在值相等的元素，返回该元素下标否则返回-1
     */
    public static int binarySearch(int[] arr, int value) {
        if (arr == null || arr.length == 0) {
            return -1;
        }
        int start = 0;
        int end = arr.length - 1;
        while (start <= end) {
            // 如果使用 (start+end)/2 在start 和 end 较大时有溢出风险
            int middle = start + ((end - start) >> 1);
            if (arr[middle] == value) {
                return middle;
            } else if (arr[middle] > value) {
                end = middle - 1;
            } else {
                start = middle + 1;
            }
        }
        return -1;
    }

}