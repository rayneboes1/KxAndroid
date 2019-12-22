/**
 * 找零钱
 */
public class LookChange {

    public static void main(String[] args) {
        int[] coins = {5, 9, 7};
        int result = new LookChange().solution(coins,11 );
        System.out.println("结果：" + result);
    }


    public int solution(int[] coins, int money) {
        if(money<1){
            return 0;
        }
        int[] table = new int[money + 1];
        for (int i = 0; i < table.length; i++) {
            table[i] = Integer.MAX_VALUE;
        }
        for (int i = 0; i < coins.length; i++) {
            if (money >= coins[i]) {
                table[coins[i]] = 1;
            }
        }
        for (int i = 1; i <= money; i++) {
            for (int j = 0; j < coins.length; j++) {
                if (i >= coins[j] && table[i - coins[j]] < Integer.MAX_VALUE) {
                    table[i] = Math.min(table[i], 1 + table[i - coins[j]]);
                }
            }
        }
        if (table[money] == Integer.MAX_VALUE) {
            return -1;
        }
        return table[money];
    }
}
