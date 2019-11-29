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

        //新链表头结点
        Node newHead = null;
        while (list != null) {
            //当前结点的下一个结点
            Node next = list.next;
            //将结点的 next 指向前一个结点
            list.next = newHead;
            //头结点迁移
            newHead = list;
            list = next;
        }
        return newHead;
    }

}
