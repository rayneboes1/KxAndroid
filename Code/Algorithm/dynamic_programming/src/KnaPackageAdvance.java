import java.util.Arrays;

/**
 * 0-1背包升级版，在满足重量约束的前提下求最大价值
 * <p>
 * 状态由三个变量组成：当前物品i，背包物品重量w，物品总价值v
 */
public class KnaPackageAdvance {

    public static void main(String[] args) {
        KnaPackageAdvance knaPackageAdvance = new KnaPackageAdvance();
        int[] weights = {1, 2, 3, 6, 9};
        int[] values = {4, 6, 2, 11, 1};
        int maxValue = knaPackageAdvance.solution(weights, values, 25);
        int maxValue2 = knaPackageAdvance.solution2(weights, values, 25);
        System.out.println("最大价值1：" + maxValue);
        System.out.println("最大价值2：" + maxValue2);
    }


    /**
     * 二维数组
     */
    public int solution(int[] weights, int[] values, int maxWeight) {
        int[][] states = new int[weights.length][maxWeight + 1];
        //初始值
        for (int i = 0; i < states.length; i++) {
            for (int j = 0; j <= maxWeight; j++) {
                states[i][j] = -1;
            }
        }
        //初始化第一个物品情况
        states[0][0] = 0;
        if (weights[0] <= maxWeight) {
            states[0][weights[0]] = values[0];
        }
        for (int i = 1; i < weights.length; i++) {
            //不放入第i个物品
            for (int j = 0; j <= maxWeight; j++) {
                if (states[i - 1][j] >= 0) {
                    states[i][j] = states[i - 1][j];
                }
            }
            //放入第i个物品
            for (int j = 0; j <= maxWeight - weights[i]; j++) {
                if (states[i - 1][j] >= 0) {
                    int value = states[i - 1][j] + values[i];
                    if (states[i][j + weights[i]] < value) {
                        states[i][j + weights[i]] = value;
                    }
                }
            }
        }
        int maxValue = 0;
        for (int i = 0; i <= maxWeight; i++) {
            if (states[weights.length - 1][i] > maxValue) {
                maxValue = states[weights.length - 1][i];
            }
        }
        for (int i = 0; i <= maxWeight; i++) {
            if (states[weights.length - 1][i] == maxValue) {
                //print reslut for i
                printResult(states, weights, values, i, maxValue);
            }
        }
//        printArray(states);
        return maxValue;
    }

    /**
     * 输出选择路径
     */
    private void printResult(int[][] state, int[] weights, int[] values, int weight, int maxValue) {
        StringBuilder sbW = new StringBuilder();
        StringBuilder sbV = new StringBuilder();
        int n = weights.length;
        for (int i = n - 1; i >= 0; i--) {
            //如果去掉当前物品后对应重量的价值刚好等于总价值减去当前物品的价值
            if (state[n - 1][weight - weights[i]] == maxValue - values[i]) {
                sbW.append(weights[i]);
                sbW.append(",");
                sbV.append(values[i]);
                sbV.append(",");
                maxValue -= values[i];
                weight -= weights[i];
            }
        }
        System.out.println("解法1选择的物品重量为：" + sbW.toString());
//        System.out.println("选择的物品价值为：" + sbV.toString());
    }

    /**
     * 使用一维数组
     * 数组中的值为某一重量下的最大价值
     */
    public int solution2(int[] weights, int[] values, int maxWeight) {
        int[] state = new int[maxWeight + 1];
        for (int i = 0; i < weights.length; i++) {
            for (int j = maxWeight - weights[i]; j >= 0; j--) {
                //如果放入物品后对应重量的价值大于之前同等重量物品的价值，则更新值为最大
                if (state[j] + values[i] > state[j + weights[i]]) {
                    state[j + weights[i]] = state[j] + values[i];
                }
            }
        }

        int max = 0;
        for (int i = 0; i <= maxWeight; i++) {
            if (state[i] > max) {
                max = state[i];
            }

        }
//        System.out.println(Arrays.toString(state));
        printResult2(weights, values, state, max);
        return max;

    }

    private void printResult2(int[] weights, int[] values, int[] state, int max) {
        //找到第一个为最大值的重量
        int weightWhenMaxWeight = 0;
        for (int i = state.length - 1; i > 0; i--) {
            if (state[i] == max) {
                weightWhenMaxWeight = i;
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = weights.length - 1; i >= 0; i--) {
            if (state[weightWhenMaxWeight - weights[i]] == max - values[i]) {
                sb.append(weights[i]);
                sb.append(",");
                max -= values[i];
                weightWhenMaxWeight -= weights[i];
            }
        }
        System.out.println("解法二选择物品："+sb.toString());
    }
}
