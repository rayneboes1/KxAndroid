import org.junit.Test;

import static org.junit.Assert.*;

public class LinkedQueueTest {

    @Test
    public void testEnqueue(){
        Queue q = new LinkedQueue(4);
        assertTrue(q.isEmpty());
        q.enqueue(1);
        q.enqueue(2);
        assertEquals(2,q.size());
        q.enqueue(3);
        q.enqueue(4);
        assertEquals(4,q.size());
        assertFalse(q.enqueue(5));
    }

    @Test
    public void testDeque(){
        Queue q  = new LinkedQueue(5);
        q.enqueue(1);
        q.enqueue(2);
        q.enqueue(3);
        assertEquals(1,q.deque());
        assertEquals(2,q.size());
        assertEquals(2,q.deque());
        assertEquals(3,q.deque());
        assertTrue(q.isEmpty());
        assertEquals(-1,q.deque());
        q.enqueue(1);
        assertEquals(1,q.size());
        q.enqueue(2);
        assertEquals(2,q.size());
        assertEquals(1,q.deque());
    }

    @Test
    public void testMoveData(){
        Queue q = new LinkedQueue(4);
        q.enqueue(1);
        q.enqueue(2);
        q.enqueue(3);
        q.enqueue(4);
        assertEquals(4,q.size());
        assertEquals(1,q.deque());
        assertEquals(3,q.size());
        assertTrue(q.enqueue(5));
        assertEquals(4,q.size());
        assertEquals(2,q.deque());
        assertEquals(3,q.deque());
        assertEquals(4,q.deque());
        assertEquals(5,q.deque());
        assertTrue(q.isEmpty());
    }

    @Test
    public void testEmpty(){
        Queue q = new LinkedQueue(0);
        assertTrue(q.isEmpty());
        assertFalse(q.enqueue(1));
        assertTrue(q.isEmpty());
    }
}
