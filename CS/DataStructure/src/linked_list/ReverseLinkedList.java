package linked_list;

/**
 * 单链表反转
 * <p>
 * 思路：从头结点开始，对每个结点采用头插法插入一个新的链表
 */
public class ReverseLinkedList {


    public static Node resolve(Node list) {
        //链表为空或只有一个结点，直接返回
        if (list == null || list.next == null) {
            return list;
        }

        Node cur = list;
        //新链表头结点
        Node newHead = null;
        while (cur != null) {
            Node next = cur.next;
            cur.next = newHead;
            newHead = cur;
            cur = next;
        }
        return newHead;
    }

}
