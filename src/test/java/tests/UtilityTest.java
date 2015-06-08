package tests;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static util.Utility.*;

public class UtilityTest {

    @Test
    public void utilityTest() {
        assertEquals("test", stripQuotes("\"test\""));

        Calendar testDate = Calendar.getInstance();
        testDate.set(1994, Calendar.NOVEMBER, 15, 12, 45, 26);
        testDate.setTimeZone(TimeZone.getTimeZone("GMT"));

        assertEquals(testDate.getTime().toString(),
                parseHTTPLastModifiedDate("Tue, 15 Nov 1994 12:45:26 GMT").toString());

        assertEquals("1994-11-15T12:45Z", formatDateISO8601(testDate.getTime()));

        assertEquals(new Date(94, 10, 15, 12, 45, 26), localDateTimeToDate(LocalDateTime.of(1994, 11, 15, 12, 45, 26)));

        assertEquals(LocalDateTime.of(1994, 11, 15, 12, 45, 26), dateToLocalDateTime(new Date(94, 10, 15, 12, 45, 26)));

        assertEquals(new Date(94, 10, 15, 12, 45, 26).getTime(), localDateTimeToLong(LocalDateTime.of(1994, 11, 15, 12, 45, 26)));

        assertEquals("HelloWorldThisIsATest", snakeCaseToCamelCase("hello_world_this_is_a_test"));

        assertEquals(1, parseVersionNumber("1.2.3a").get()[0]);
        assertEquals(2, parseVersionNumber("1.2.3a").get()[1]);
        assertEquals(3, parseVersionNumber("1.2.3a").get()[2]);
    }

}
