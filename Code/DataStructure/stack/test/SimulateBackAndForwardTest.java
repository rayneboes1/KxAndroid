import static org.junit.Assert.*;
import org.junit.Test;

public class SimulateBackAndForwardTest {

    @Test
    public void testView(){
        SimulateBackAndForward s = new SimulateBackAndForward(30);
        assertTrue(s.back.isEmpty());
        assertTrue(s.forward.isEmpty());
        s.view(1);
        s.view(2);
        s.view(3);
        assertEquals(3,s.back.size());
        assertTrue(s.forward.isEmpty());
    }

    @Test
    public void testBack(){
        SimulateBackAndForward s = new SimulateBackAndForward(20);
        s.view(1);
        s.view(2);
        s.view(3);
        assertEquals(3,s.back());
        assertEquals(1,s.forward.size());
        assertEquals(2,s.back());
        assertEquals(2,s.forward.size());
        assertEquals(-1,s.back());
        assertEquals(1,s.back.size());
        assertEquals(2,s.forward.size());
        s.view(4);
        assertEquals(2,s.back.size());
        assertEquals(0,s.forward.size());
        assertEquals(4,s.back());
    }

    @Test
    public void testForward(){
        SimulateBackAndForward s = new SimulateBackAndForward(20);
        s.view(1);
        s.view(2);
        s.view(3);
        assertEquals(-1,s.forward());
        s.back();
        assertEquals(3,s.forward());
        assertEquals(0,s.forward.size());
        assertEquals(3,s.back.size());
        assertEquals(-1,s.forward());

        s.back();
        s.back();
        s.back();
        assertEquals(1,s.back.size());
        assertEquals(2,s.forward.size());
        assertEquals(2,s.forward());
        assertEquals(3,s.forward());
        assertEquals(0,s.forward.size());
        assertEquals(3,s.back.size());


    }
}
