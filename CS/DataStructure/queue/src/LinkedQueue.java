public class LinkedQueue implements Queue {
    private int initCap;
    private int size;
    //空数据的头结点，用于简化操作
    private Node head = new Node(-1);
    private Node tail = head;
    private Node tailPre;


    public LinkedQueue(int initCap) {
        this.initCap = initCap;
        size = 0;
    }

    @Override
    public boolean enqueue(int data) {
        if (size == initCap) {
            return false;
        }
        Node n = new Node(data);
        n.next = head.next;
        head.next = n;
        if (head == tail) {
            tail = n;
        }
        size++;
        return true;
    }

    @Override
    public int deque() {
        if (isEmpty()) {
            return -1;
        }
        int v = tail.data;
        Node pre = head;
        while (pre != null) {
            if (pre.next == tail) {
                break;
            }
            pre = pre.next;
        }
        assert pre != null;
        pre.next = null;
        tail = pre;
        size--;
        return v;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        tail = head;
        Node n = head;
        while (n.next != null) {
            Node t = n.next;
            n.next = t.next;
            t.next = null;
        }
        size = 0;
    }


    static class Node {
        int data;
        Node next;

        Node(int data) {
            this.data = data;
        }
    }
}
