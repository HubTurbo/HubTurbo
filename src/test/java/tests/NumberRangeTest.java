package tests;

import static org.junit.Assert.*;

import org.junit.Test;

import filter.expression.NumberRange;

public class NumberRangeTest {

    @Test
    public void numberRangeTest() {
        NumberRange onetotwo = new NumberRange(1, 2);
        NumberRange onetotwostrict = new NumberRange(1, 2, true);

        assertEquals(1, (long) onetotwo.getStart());
        assertEquals(2, (long) onetotwo.getEnd());
        assertTrue(onetotwo.encloses(1));
        assertTrue(onetotwo.encloses(2));
        assertEquals(1, (long) onetotwostrict.getStart());
        assertEquals(2, (long) onetotwostrict.getEnd());
        assertFalse(onetotwostrict.encloses(1));
        assertFalse(onetotwostrict.encloses(2));
        assertNotEquals(onetotwo.hashCode(), onetotwostrict.hashCode());
        assertNotEquals(onetotwo, onetotwostrict);
    }
}
