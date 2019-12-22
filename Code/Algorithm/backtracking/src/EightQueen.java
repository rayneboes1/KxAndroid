/**
 * 八皇后问题
 */
public class EightQueen {
    /**
     * 标识第i行放置在第j列
     */
    private int[] placeQueen = new int[8];

    public static void main(String[] args) {
        EightQueen eightQueen = new EightQueen();
        eightQueen.cal8queen(0);
    }


    private void cal8queen(int row) {
        if (row == placeQueen.length) {
            //递归结束
            printQueens(placeQueen);
            return;
        }
        for (int i = 0; i < placeQueen.length; i++) {
            //检查该行的每一列是否满足要求
            if (isOk(row, i)) {
                placeQueen[row] = i;
                //检查下一行
                cal8queen(row + 1);
            }
        }
    }

    private boolean isOk(int row, int column) {
        int leftUp = column - 1;
        int rightUp = column + 1;
        for (int j = row - 1; j >= 0; j--) {
            //左边不用检测
            //检查上边
            if (placeQueen[j] == column) {
                return false;
            }

            //检查左上角
            if (leftUp >= 0) {
                if (placeQueen[j] == leftUp) {
                    return false;
                }
            }
            //检查右上角
            if (rightUp >= 0) {
                if (placeQueen[j] == rightUp) {
                    return false;
                }
            }
            leftUp--;
            rightUp++;

        }
        return true;
    }

    private void printQueens(int[] placeQueen) {
        System.out.println("------BEGIN----");
        for (int i = 0; i < placeQueen.length; i++) {
            for (int j = 0; j < placeQueen.length; j++) {
                if (placeQueen[i] == j) {
                    System.out.print("Q ");
                } else {
                    System.out.print("* ");
                }
            }
            System.out.println();
        }
        System.out.println("------END------");
    }
}
