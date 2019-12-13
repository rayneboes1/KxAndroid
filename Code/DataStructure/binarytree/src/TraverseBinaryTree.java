import java.util.LinkedList;
import java.util.Queue;

/**
 * 二叉树遍历
 */
class TraverseBinaryTree {

    public static void main(String[] args) {

        /*
         *       1
         *    2    3
         * 4     5    6
         */
        TreeNode root = new TreeNode(1);
        root.left = new TreeNode(2);
        root.right = new TreeNode(3);
        root.left.left = new TreeNode(4);
        root.right.left = new TreeNode(5);
        root.right.right = new TreeNode(6);

//        preOrder(root);//1 2 4 5 3 6
//        inOrder(root);//4 2 1 5 3 6
//        postOrder(root);//4 2 5 6 3 1
        levelOrder(root);//1 2 3 4 5 6

    }

    /**
     * 前序遍历
     *
     * @param root 树的根节点
     */
    static void preOrder(TreeNode root) {
        if (root == null) {
            return;
        }
        //先遍历根节点
        System.out.println(root.value);
        preOrder(root.left);
        preOrder(root.right);
    }


    /**
     * 中序遍历
     *
     * @param root 树的根节点
     */
    static void inOrder(TreeNode root) {
        if (root == null) {
            return;
        }
        inOrder(root.left);
        System.out.println(root.value);
        inOrder(root.right);
    }


    /**
     * 后序遍历
     *
     * @param root 树的根节点
     */
    static void postOrder(TreeNode root) {
        if (root == null) {
            return;
        }
        postOrder(root.left);
        postOrder(root.right);
        System.out.println(root.value);
    }


    /**
     * 层序遍历
     *
     * @param root 树的根节点
     */
    static void levelOrder(TreeNode root) {
        if (root == null) {
            return;
        }
        Queue<TreeNode> q = new LinkedList<>();
        q.offer(root);
        while (!q.isEmpty()) {
            TreeNode n = q.poll();
            System.out.println(n.value);
            if (n.left != null) {
                q.offer(n.left);
            }
            if (n.right != null) {
                q.offer(n.right);
            }
        }

    }


}