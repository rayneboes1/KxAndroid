import static org.junit.Assert.*;
import org.junit.Test;

public class FibonacciTest {

    @Test
    public void test(){
        assertEquals(1,Fibonacci.resolution(1));
        assertEquals(1,Fibonacci.resolution(2));
        assertEquals(2,Fibonacci.resolution(3));
        assertEquals(3,Fibonacci.resolution(4));
        assertEquals(5,Fibonacci.resolution(5));
        assertEquals(8,Fibonacci.resolution(6));
    }
}
