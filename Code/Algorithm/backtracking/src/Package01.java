/**
 * 0-1 背包问题回溯解法
 */
public class Package01 {

    public static void main(String[] args) {
        Package01 package01 = new Package01();

        int[] items = { 1, 3, 5, 7, 8, 9, 11, 20, 12 };
        int result = package01.solution(items, 74);
        System.out.println("最大重量=" + result);

    }

    private int maxWeight = Integer.MIN_VALUE;

    public int solution(int[] items, int packageWeight) {
        maxWeight = Integer.MIN_VALUE;
        solutionInternal(items, 0, 0, packageWeight);
        return maxWeight;
    }

    private void solutionInternal(int[] items, int i, int curWeight, int packageWeight) {
        if (curWeight == packageWeight || i == items.length) {
            // 已经到最后一个物品了
            if (curWeight > maxWeight) {
                maxWeight = curWeight;
            }
            return;
        }
        // 不放入第i个物品
        solutionInternal(items, i + 1, curWeight, packageWeight);
        // 放入第i个物品,对不符合条件的分支直接剪掉
        if (curWeight + items[i] <= packageWeight) {
            solutionInternal(items, i + 1, curWeight + items[i], packageWeight);
        }
    }
}
