package tests;

import filter.expression.NumberRange;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class NumberRangeTest {

    @Test
    public void numberRangeTest() {
        NumberRange onetotwo = new NumberRange(1, 2);
        NumberRange onetotwostrict = new NumberRange(1, 2, true);
        NumberRange nulltotwo = new NumberRange(null, 2);
        NumberRange onetonull = new NumberRange(1, null);
        NumberRange zerototwo = new NumberRange(0, 2);

        assertEquals(1, (long) onetotwo.getStart());
        assertEquals(2, (long) onetotwo.getEnd());
        assertEquals(true, onetotwo.encloses(1));
        assertEquals(true, onetotwo.encloses(2));
        assertEquals(1, (long) onetotwostrict.getStart());
        assertEquals(2, (long) onetotwostrict.getEnd());
        assertEquals(false, onetotwostrict.encloses(1));
        assertEquals(false, onetotwostrict.encloses(2));
        assertNotEquals(onetotwo.hashCode(), onetotwostrict.hashCode());
        assertNotEquals(onetotwo, onetotwostrict);
        assertEquals(true, onetotwo.equals(onetotwo));
        assertEquals(false, onetotwo.equals(null));
        assertEquals(false, onetotwo.equals(""));
        assertEquals(false, onetonull.equals(nulltotwo));
        assertEquals(false, nulltotwo.equals(onetonull));
        assertEquals(false, zerototwo.equals(onetotwo));
    }
}
