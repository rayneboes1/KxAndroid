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
        Node reverse = ReverseLinkedList.resolution(list);
        assertEquals("4,3,2,1,",Node.link2String(reverse));
    }

    @Test
    public void testReverseWithNull(){
        Node reverse = ReverseLinkedList.resolution(null);
        assertEquals("",Node.link2String(reverse));
    }

    @Test
    public void testReverseWithOneNode(){
        Node list = new Node(1);
        assertEquals("1,",Node.link2String(list));
        Node reverse = ReverseLinkedList.resolution(list);
        assertEquals("1,",Node.link2String(reverse));
    }

    @Test
    public void testReverseWith2Node(){
        Node list = new Node(1);
        list.next = new Node(2);
        assertEquals("1,2,",Node.link2String(list));
        Node reverse = ReverseLinkedList.resolution(list);
        assertEquals("2,1,",Node.link2String(reverse));
    }

    @Test
    public void  testReverseByRecursion(){
        Node list = new Node(1);
        list.next = new Node(2);
        list.next.next = new Node(3);
        list.next.next.next = new Node(4);
        assertEquals("1,2,3,4,",Node.link2String(list));
        Node reverse = ReverseLinkedList.resolutionByRecursion(list);
        assertEquals("4,3,2,1,",Node.link2String(reverse));
    }

    @Test
    public void testReverseByRecursionWithNull(){
        Node reverse = ReverseLinkedList.resolutionByRecursion(null);
        assertEquals("",Node.link2String(reverse));
    }

    @Test
    public void testReverseByRecursionWithOneNode(){
        Node list = new Node(1);
        assertEquals("1,",Node.link2String(list));
        Node reverse = ReverseLinkedList.resolutionByRecursion(list);
        assertEquals("1,",Node.link2String(reverse));
    }

    @Test
    public void testReverseByRecursionWith2Node(){
        Node list = new Node(1);
        list.next = new Node(2);
        assertEquals("1,2,",Node.link2String(list));
        Node reverse = ReverseLinkedList.resolutionByRecursion(list);
        assertEquals("2,1,",Node.link2String(reverse));
    }
}
