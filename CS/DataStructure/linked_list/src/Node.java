
public class Node {
    int data;
    Node next;

    public Node(int data) {
        this.data = data;
    }

    public static String link2String(Node head) {
        if (head == null) {
            return "";
        }
        String result = head.data + "";
        result += "," + link2String(head.next);
        return result;
    }
}
