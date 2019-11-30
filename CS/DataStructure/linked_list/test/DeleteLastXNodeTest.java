import static org.junit.Assert.*;
import org.junit.Test;

public class DeleteLastXNodeTest {

    @Test
    public void testWithNull(){
        assertEquals(null,DeleteLastXNode.resolution(null,3));
    }

    @Test
    public void testWithShort(){
        Node n1 = new Node(1);
        n1.next = new Node(2);
        n1.next.next = new Node(3);
        n1.next.next.next = new Node(4);
        n1.next.next.next.next = new Node(5);

        assertEquals("1,2,3,4,5,",Node.link2String(DeleteLastXNode.resolution(n1,6)));
    }

    @Test
    public void testNormal(){
        Node n1 = new Node(1);
        n1.next = new Node(2);
        n1.next.next = new Node(3);
        n1.next.next.next = new Node(4);
        n1.next.next.next.next = new Node(5);

    }

    @Test
    public void testWithOneNode(){
        Node n1 = new Node(1);
        assertEquals("",Node.link2String(DeleteLastXNode.resolution(n1,1)));
    }

    @Test
    public void testWithOneNodeShort(){
        Node n1 = new Node(1);
        assertEquals("1,",Node.link2String(DeleteLastXNode.resolution(n1,3)));
    }

    @Test
    public void testDeleteHead(){
        Node n1 = new Node(1);
        n1.next = new Node(2);
        n1.next.next = new Node(3);
        assertEquals("2,3,",Node.link2String(DeleteLastXNode.resolution(n1,3)));
    }

    @Test
    public void testDeleteTail(){
        Node n1 = new Node(1);
        n1.next = new Node(2);
        n1.next.next = new Node(3);
        assertEquals("1,2,",Node.link2String(DeleteLastXNode.resolution(n1,1)));
    }

    @Test
    public void testDeleteLast0(){
        Node n1 = new Node(1);
        n1.next = new Node(2);
        n1.next.next = new Node(3);
        assertEquals("1,2,3,",Node.link2String(DeleteLastXNode.resolution(n1,0)));

    }
}
