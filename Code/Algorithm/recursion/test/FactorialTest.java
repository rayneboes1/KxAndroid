import static org.junit.Assert.*;
import org.junit.Test;

public class FactorialTest {

    @Test
    public void test(){
        assertEquals(1,Factorial.resolution(0));
        assertEquals(1,Factorial.resolution(1));
        assertEquals(2,Factorial.resolution(2));
        assertEquals(6,Factorial.resolution(3));
        assertEquals(24,Factorial.resolution(4));
    }
}
