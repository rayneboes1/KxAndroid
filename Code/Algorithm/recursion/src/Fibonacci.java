public class Fibonacci {


    public static int resolution(int n) {
        if (n == 1) {
            return 1;
        }
        if (n == 2) {
            return 1;
        }
        return resolution(n - 1) + resolution(n - 2);
    }
}
