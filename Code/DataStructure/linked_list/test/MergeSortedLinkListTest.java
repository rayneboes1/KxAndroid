import static org.junit.Assert.*;

import org.junit.Test;

public class MergeSortedLinkListTest {

    @Test
    public void testResolutionWithNull() {
        assertNull(MergeSortedLinkedList.resolution(null, null));

        Node n1 = new Node(1);
        assertEquals(n1, MergeSortedLinkedList.resolution(null, n1));
        assertEquals(n1, MergeSortedLinkedList.resolution(n1, null));
    }

    @Test
    public void testResolutionRecursionWithNull() {
        assertNull(MergeSortedLinkedList.resolutionByRecursion(null, null));

        Node n1 = new Node(1);
        assertEquals(n1, MergeSortedLinkedList.resolutionByRecursion(null, n1));
        assertEquals(n1, MergeSortedLinkedList.resolutionByRecursion(n1, null));

    }

    @Test
    public void testResolutionWithOneNode() {
        Node n1 = new Node(1);
        Node n2 = new Node(2);
        assertEquals("1,2,", Node.link2String(MergeSortedLinkedList.resolution(n2, n1)));
    }

    @Test
    public void testResolutionRecursionWithOneNode() {
        Node n1 = new Node(1);
        Node n2 = new Node(2);
        assertEquals("1,2,", Node.link2String(MergeSortedLinkedList.resolutionByRecursion(n2, n1)));
    }

    @Test
    public void testResolution() {
        Node n1 = new Node(1);
        n1.next = new Node(3);
        n1.next.next = new Node(5);
        n1.next.next.next = new Node(7);
        n1.next.next.next.next = new Node(10);

        Node p1 = new Node(2);
        p1.next = new Node(4);
//        p1.next.next = new Node(6);
//        p1.next.next.next = new Node(8);
//        p1.next.next.next.next = new Node(9);

        assertEquals("1,2,3,4,5,7,10,", Node.link2String(MergeSortedLinkedList.resolution(n1, p1)));
    }


    @Test
    public void testResolutionRecursion() {
        Node n1 = new Node(1);
        n1.next = new Node(3);
        n1.next.next = new Node(5);
        n1.next.next.next = new Node(7);
        n1.next.next.next.next = new Node(10);

        Node p1 = new Node(1);
        p1.next = new Node(4);
        p1.next.next = new Node(6);
//        p1.next.next.next = new Node(8);
//        p1.next.next.next.next = new Node(9);

        assertEquals("1,1,3,4,5,6,7,10,", Node.link2String(MergeSortedLinkedList.resolutionByRecursion(n1, p1)));
    }

    @Test
    public void testSimpleResolution() {
        Node n1 = new Node(1);
        n1.next = new Node(3);
        n1.next.next = new Node(5);
        n1.next.next.next = new Node(7);
        n1.next.next.next.next = new Node(10);

        Node p1 = new Node(1);
        p1.next = new Node(4);
        p1.next.next = new Node(6);
//        p1.next.next.next = new Node(8);
//        p1.next.next.next.next = new Node(9);

        assertEquals("1,1,3,4,5,6,7,10,", Node.link2String(MergeSortedLinkedList.resolutionSimple(n1, p1)));
    }


}
