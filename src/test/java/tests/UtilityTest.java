package tests;

import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static util.Utility.parseHTTPLastModifiedDate;
import static util.Utility.stripQuotes;

public class UtilityTest {

    @Test
    public void utilityTest() {
        assertEquals("test", stripQuotes("\"test\""));

        Calendar testDate = Calendar.getInstance();
        testDate.set(1994, Calendar.NOVEMBER, 15, 12, 45, 26);
        testDate.setTimeZone(TimeZone.getTimeZone("GMT"));
        assertEquals(testDate.getTime().toString(),
                parseHTTPLastModifiedDate("Tue, 15 Nov 1994 12:45:26 GMT").toString());


    }

}
