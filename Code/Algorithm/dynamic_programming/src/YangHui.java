/**
 * 杨辉三角求最短路径
 */
public class YangHui {

    public static void main(String[] args) {
        int[][] yanghui = {{5},{7,8},{2,3,4},{4,9,6,1},{2,7,9,4,5}};
        int result = new YangHui().solution(yanghui);
        System.out.println("最短路径长度为："+result);
    }


    public int solution(int[][] yanghui) {
        int[][] states = new int[yanghui.length][yanghui.length];
        int sumR=0;
        int sumC = 0;
        for (int i = 0; i < yanghui.length; i++) {
            sumR+=yanghui[i][yanghui[i].length - 1];
            states[i][i] = sumR;
            sumC+=yanghui[i][0];
            states[i][0] = sumC;
        }

        for (int i = 1; i < yanghui.length; i++) {
            for (int j = 1; j < yanghui[i].length-1; j++) {
                states[i][j] = Math.min(states[i-1][j - 1] + yanghui[i][j],
                        states[i - 1][j] + yanghui[i][j]);
            }
        }
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < states[yanghui.length - 1].length; i++) {
            if (states[yanghui.length - 1][i] < min) {
                min = states[yanghui.length - 1][i];
            }

        }
        Util.printArray(states);

        return min;
    }
}
