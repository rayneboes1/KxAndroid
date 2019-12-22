/**
 * 0-1背包简单版（最大重量）
 */
public class KnaPackage {

    public static void main(String[] args) {
        int[] items = {1,2,3,5,4,7,9,11,13};
        KnaPackage knaPackage = new KnaPackage();
        int result = knaPackage.solution(items,44);
        System.out.println("最大重量："+result);
    }


    public int solution(int[] items, int maxWeight) {
        boolean[][] states = new boolean[items.length][maxWeight + 1];
        states[0][0] = true;
        if (items[0] <= maxWeight) {
            states[0][items[0]] = true;
        }
        for (int i = 1; i < items.length; i++) {
            //不把第i个物品放入
            for (int j = 0; j <= maxWeight; j++) {
                if (states[i - 1][j]) {
                    states[i][j] = states[i - 1][j];
                }
            }
            //将第i个物品放入
            for (int j = 0; j <= maxWeight - items[i]; j++) {
                if (states[i - 1][j]) {
                    states[i][j + items[i]] = true;
                }
            }
        }
        for (int i = maxWeight; i >=0 ; i--) {
            if(states[items.length-1][i]){
                return i;
            }
        }
        return 0;
    }
}
