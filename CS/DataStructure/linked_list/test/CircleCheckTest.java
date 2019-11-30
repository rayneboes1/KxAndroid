
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class CircleCheckTest {
    private Node nullList;
    //只有一个结点的链表
    private Node oneNodeList;
    //只有一个结点的有环链表
    private Node oneNodeCircleList;

    //有两个结点的链表
    private Node twoNodeList;
    //有两个结点的有环链表
    private Node twoNodeCircleList;

    //有多个结点的链表
    private Node normalCircleList;
    //有多个结点的有环链表
    private Node normalList;

    @Before
    public void init() {
        nullList = null;

        oneNodeList = new Node(1);

        oneNodeCircleList = new Node(1);
        oneNodeCircleList.next = oneNodeCircleList;

        twoNodeList = new Node(1);
        twoNodeList.next = new Node(2);

        twoNodeCircleList = new Node(1);
        twoNodeCircleList.next = new Node(2);
        //第二个结点单独成环
        twoNodeCircleList.next.next = twoNodeCircleList.next;

        normalList = new Node(1);
        normalList.next = new Node(2);
        normalList.next.next = new Node(3);
        normalList.next.next.next = new Node(4);
        normalList.next.next.next.next = new Node(5);

        normalCircleList = new Node(1);
        normalCircleList.next = new Node(2);
        normalCircleList.next.next = new Node(3);
        Node p = new Node(4);
        normalCircleList.next.next.next = p;
        p.next = new Node(5);
        p.next.next = new Node(6);
        p.next.next.next = p;

    }


    @Test
    public void testHasCircleWithNull() {
        assertFalse(CircleCheck.hasCircle(nullList));

        assertEquals(0, CircleCheck.circleLength(nullList));

        assertNull(CircleCheck.circleEntrance(nullList));

        assertEquals(0,CircleCheck.lengthBeforeCircleEntrance(nullList));
    }

    @Test
    public void testHasCircleWithOneNode() {
        //只有一个结点，没有环
        assertFalse(CircleCheck.hasCircle(oneNodeList));
        assertEquals(0, CircleCheck.circleLength(oneNodeList));
        assertNull(CircleCheck.circleEntrance(oneNodeList));
        assertEquals(1,CircleCheck.lengthBeforeCircleEntrance(oneNodeList));
        //单结点成环
        assertTrue(CircleCheck.hasCircle(oneNodeCircleList));
        assertEquals(1, CircleCheck.circleLength(oneNodeCircleList));
        assertEquals(1,CircleCheck.circleEntrance(oneNodeCircleList).data);
        assertEquals(0,CircleCheck.lengthBeforeCircleEntrance(oneNodeCircleList));
    }

    @Test
    public void testHasCircleWithTwoNode() {
        assertFalse(CircleCheck.hasCircle(twoNodeList));
        assertEquals(0,CircleCheck.circleLength(twoNodeList));
        assertNull(CircleCheck.circleEntrance(twoNodeList));
        assertEquals(2,CircleCheck.lengthBeforeCircleEntrance(twoNodeList));

        assertTrue(CircleCheck.hasCircle(twoNodeCircleList));
        assertEquals(1,CircleCheck.circleLength(twoNodeCircleList));
        assertEquals(2,CircleCheck.circleEntrance(twoNodeCircleList).data);
        assertEquals(1,CircleCheck.lengthBeforeCircleEntrance(twoNodeCircleList));
    }


    @Test
    public void testHasCircle() {
        assertFalse(CircleCheck.hasCircle(normalList));
        assertEquals(0,CircleCheck.circleLength(normalList));
        assertNull(CircleCheck.circleEntrance(normalList));
        assertEquals(5,CircleCheck.lengthBeforeCircleEntrance(normalList));

        assertTrue(CircleCheck.hasCircle(normalCircleList));
        assertEquals(3,CircleCheck.circleLength(normalCircleList));
        assertEquals(4,CircleCheck.circleEntrance(normalCircleList).data);
        assertEquals(3,CircleCheck.lengthBeforeCircleEntrance(normalCircleList));
    }


}
