package linked_list;

import org.junit.Test;
import static org.junit.Assert.*;

public class MiddleNodeTest {

    @Test
    public void testResolution1WithNull(){
        assertEquals("",Node.link2String(MiddleNode.resolution1(null)));
    }

    @Test
    public void testResolution1WithOneNode(){
        assertEquals("1,",Node.link2String(MiddleNode.resolution1(new Node(1))));
    }

    @Test
    public void testResolution1WithTwoNode(){
        Node n1  = new Node(1);
        n1.next = new Node(2);
        assertEquals("2,",Node.link2String(MiddleNode.resolution1(n1)));
    }

    @Test
    public void testResolution1WithOddNode(){
        Node n1 = new Node(1);
        Node n2 = new Node(2);
        Node n3 = new Node(3);
        Node n4 = new Node(4);
        Node n5 = new Node(5);
        n1.next = n2;
        n2.next = n3;
        n3.next = n4;
        n4.next = n5;

        assertEquals("3,4,5,",Node.link2String(MiddleNode.resolution1(n1)));
    }

    @Test
    public void testResolution1WithEvenNode(){
        Node n1 = new Node(1);
        Node n2 = new Node(2);
        Node n3 = new Node(3);
        Node n4 = new Node(4);
        Node n5 = new Node(5);
        Node n6 = new Node(6);
        n1.next = n2;
        n2.next = n3;
        n3.next = n4;
        n4.next = n5;
        n5.next = n6;
        assertEquals("4,5,6,",Node.link2String(MiddleNode.resolution1(n1)));
    }




    @Test
    public void testResolution2WithNull(){
        assertEquals("",Node.link2String(MiddleNode.resolution2(null)));
    }


    @Test
    public void testResolution2WithOneNode(){
        assertEquals("1,",Node.link2String(MiddleNode.resolution2(new Node(1))));
    }

    @Test
    public void testResolution2WithTwoNode(){
        Node n1  = new Node(1);
        n1.next = new Node(2);
        assertEquals("1,2,",Node.link2String(MiddleNode.resolution2(n1)));
    }

    @Test
    public void testResolution2WithOddNode(){

        Node n1 = new Node(1);
        Node n2 = new Node(2);
        Node n3 = new Node(3);
        Node n4 = new Node(4);
        Node n5 = new Node(5);
        n1.next = n2;
        n2.next = n3;
        n3.next = n4;
        n4.next = n5;

        assertEquals("3,4,5,",Node.link2String(MiddleNode.resolution2(n1)));

    }

    @Test
    public void testResolution2WithEvenNode(){

        Node n1 = new Node(1);
        Node n2 = new Node(2);
        Node n3 = new Node(3);
        Node n4 = new Node(4);
        Node n5 = new Node(5);
        Node n6 = new Node(6);
        n1.next = n2;
        n2.next = n3;
        n3.next = n4;
        n4.next = n5;
        n5.next = n6;
        assertEquals("3,4,5,6,",Node.link2String(MiddleNode.resolution2(n1)));

    }
}
