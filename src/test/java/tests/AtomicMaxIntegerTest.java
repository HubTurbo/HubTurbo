package tests;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;

import util.AtomicMaxInteger;

public class AtomicMaxIntegerTest {

    @Test
    public void atomicMaxIntegerTest() {
        AtomicMaxInteger n = new AtomicMaxInteger(0);

        for (int i = 0; i < 5; i++) {
            n.increment();
        }

        for (int i = 0; i < 2; i++) {
            n.decrement();
        }

        assertEquals(3, n.get());
        assertEquals(5, n.getMax());
    }
}
