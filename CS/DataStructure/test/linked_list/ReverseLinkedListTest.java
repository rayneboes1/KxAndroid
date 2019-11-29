package linked_list;


import static org.junit.Assert.*;
import org.junit.Test;

public class ReverseLinkedListTest {


    @Test
    public void testReverse(){
        Node list = new Node(1);
        list.next = new Node(2);
        list.next.next = new Node(3);
        list.next.next.next = new Node(4);
        assertEquals("1,2,3,4,",Node.link2String(list));
        Node reverse = ReverseLinkedList.resolve(list);
        assertEquals("4,3,2,1,",Node.link2String(reverse));
    }

    @Test
    public void testReverseWithNull(){
        Node reverse = ReverseLinkedList.resolve(null);
        assertEquals("",Node.link2String(reverse));
    }

    @Test
    public void testReverseWithOneNode(){
        Node list = new Node(1);
        assertEquals("1,",Node.link2String(list));
        Node reverse = ReverseLinkedList.resolve(list);
        assertEquals("1,",Node.link2String(reverse));
    }
}
