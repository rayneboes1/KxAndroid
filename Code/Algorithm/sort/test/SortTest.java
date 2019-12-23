import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

public class SortTest {

    private Random random;

    @Before
    public void init() {
        random = new Random();
    }


    @Test
    public void testBubbleSort() {
        for (int i = 0; i < 100; i++) {
            int[] arr = randomArray();
            int[] arrCopy = Arrays.copyOf(arr, arr.length);
            ISort sort = new BubbleSort();
            sort.sort(arr);
            Arrays.sort(arrCopy);
            assertEquals(Arrays.toString(arr), Arrays.toString(arrCopy));
        }
    }

    @Test
    public void testInsertSort(){
        for (int i = 0; i < 100; i++) {
            int[] arr = randomArray();
            int[] arrCopy = Arrays.copyOf(arr, arr.length);
            ISort sort = new InsertSort();
            sort.sort(arr);
            Arrays.sort(arrCopy);
            assertEquals(Arrays.toString(arrCopy), Arrays.toString(arr));
        }

    }

    @Test
    public void testSelectSort(){
        for (int i = 0; i < 100; i++) {
            int[] arr = randomArray();
            int[] arrCopy = Arrays.copyOf(arr, arr.length);
            ISort sort = new SelectSort();
            sort.sort(arr);
            Arrays.sort(arrCopy);
            assertEquals(Arrays.toString(arrCopy), Arrays.toString(arr));
        }

    }

    @Test
    public void testMergeSort(){
        for (int i = 0; i < 100; i++) {
            int[] arr = randomArray();
            int[] arrCopy = Arrays.copyOf(arr, arr.length);
            ISort sort = new MergeSort();
            sort.sort(arr);
            Arrays.sort(arrCopy);
            assertEquals(Arrays.toString(arrCopy), Arrays.toString(arr));
        }
    }

    @Test
    public void testQuickSort(){
        for (int i = 0; i < 100; i++) {
            int[] arr = randomArray();
            int[] arrCopy = Arrays.copyOf(arr, arr.length);
            ISort sort = new QuickSort();
            sort.sort(arr);
            Arrays.sort(arrCopy);
            assertEquals(Arrays.toString(arrCopy), Arrays.toString(arr));
        }
    }

    @Test
    public void testHeapSort(){
        for (int i = 0; i < 100; i++) {
            int[] arr = randomArrayExclude0();
            int[] arrCopy = Arrays.copyOf(arr, arr.length);
            ISort sort = new HeapSort();
            sort.sort(arr);
            Arrays.sort(arrCopy);
            assertEquals(Arrays.toString(arrCopy), Arrays.toString(arr));
        }
    }

    @Test
    public void testShellSort(){
        for (int i = 0; i < 100; i++) {
            int[] arr = randomArray();
            int[] arrCopy = Arrays.copyOf(arr, arr.length);
            ISort sort = new ShellSort();
            sort.sort(arr);
            Arrays.sort(arrCopy);
            assertEquals(Arrays.toString(arrCopy), Arrays.toString(arr));
        }
    }


    private int[] randomArray() {
        int[] arr = new int[random.nextInt(100)];
        for (int j = 0; j < arr.length; j++) {
            arr[j] = random.nextInt(200);
        }
        return arr;
    }

    private int[] randomArrayExclude0() {
        int[] arr = new int[random.nextInt(100)];
        for (int j = 1; j < arr.length; j++) {
            arr[j] = random.nextInt(200);
        }
        return arr;
    }


}
