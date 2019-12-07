/**
 * 阶乘的递归实现
 */

public class Factorial {

    public static int resolution(int n) {
        if (n == 0) {
            return 1;
        }
        if (n == 1) {
            return 1;
        }
        return n * resolution(n - 1);
    }
}
