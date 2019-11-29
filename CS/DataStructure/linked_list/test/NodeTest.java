
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class NodeTest {
    private Node list;

    @Before
    public void initList(){
        list = new Node(1);
        list.next = new Node(2);
        list.next.next = new Node(3);
        list.next.next.next=new Node(4);
    }


    @Test
    public void testNode2String(){
        assertEquals("1,2,3,4,",Node.link2String(list));
    }

    @Test
    public void testNode2StringWithNull(){
        assertEquals("",Node.link2String(null));
    }

    @Test
    public void testNode2StringWithOneNode(){
        assertEquals("1,",Node.link2String(new Node(1)));
    }

}
