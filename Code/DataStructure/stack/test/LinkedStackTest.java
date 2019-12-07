import org.junit.Test;

import static org.junit.Assert.*;

public class LinkedStackTest {

    @Test
    public void testToString() {
        Stack stack1 = new LinkedStack(0);
        assertEquals("", stack1.toString());
        Stack stack2 = new LinkedStack(1);
        stack2.push(1);
        assertEquals("1,", stack2.toString());

        Stack stack3 = new LinkedStack(4);
        stack3.push(1);
        stack3.push(2);
        stack3.push(3);
        assertEquals("3,2,1,", stack3.toString());
        stack3.pop();
        assertEquals("2,1,", stack3.toString());

    }


    @Test
    public void testPush() {
        Stack stack1 = new LinkedStack(0);
        assertFalse(stack1.push(1));

        Stack stack2 = new LinkedStack(1);
        assertTrue(stack2.push(1));
        assertFalse(stack2.push(2));

        Stack stack3 = new LinkedStack(4);
        assertTrue(stack3.push(1));
        assertTrue(stack3.push(2));
        assertTrue(stack3.push(3));
        assertTrue(stack3.push(4));
        assertEquals("4,3,2,1,", stack3.toString());
        assertFalse(stack3.push(4));
        assertEquals("4,3,2,1,", stack3.toString());
    }


    @Test
    public void testPop() {
        Stack stack1 = new LinkedStack(0);
        assertEquals(-1,stack1.pop());

        Stack stack2 = new LinkedStack(1);
        stack2.push(1);
        assertEquals(1,stack2.pop());

        Stack stack3 = new LinkedStack(4);
        stack3.push(1);
        stack3.push(2);
        stack3.push(3);
        assertEquals(3,stack3.pop());
        assertEquals(2,stack3.pop());
        assertEquals(1,stack3.pop());

    }


    @Test
    public void testSize() {

        Stack stack1 = new LinkedStack(0);
        assertEquals(0,stack1.size());

        Stack stack2 = new LinkedStack(1);
        assertEquals(0,stack2.size());
        stack2.push(1);
        assertEquals(1,stack2.size());
        stack2.pop();
        assertEquals(0,stack2.size());

        Stack stack3 = new LinkedStack(3);
        stack3.push(1);
        stack3.push(2);
        stack3.push(3);
        assertEquals(3,stack3.size());
        stack3.pop();
        stack3.pop();
        assertEquals(1,stack3.size());

    }


    @Test
    public void testClear() {
        Stack stack = new LinkedStack(4);
        assertEquals(0,stack.size());
        stack.push(1);
        stack.push(2);
        stack.push(3);
        assertEquals(3,stack.size());
        stack.clear();
        assertEquals(0,stack.size());
        assertEquals(-1,stack.pop());
        stack.push(1);
        stack.push(2);
        assertEquals(2,stack.size());
        assertEquals("2,1,",stack.toString());
    }

    @Test
    public void testIsEmpty() {
        Stack stack = new LinkedStack(4);
        assertTrue(stack.isEmpty());
        stack.push(1);
        assertFalse(stack.isEmpty());
        stack.push(2);
        stack.pop();
        stack.pop();
        assertTrue(stack.isEmpty());
        stack.push(1);
        stack.push(2);
        assertFalse(stack.isEmpty());
        stack.clear();
        assertTrue(stack.isEmpty());
    }

}
