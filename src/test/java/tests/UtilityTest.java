package tests;

import static org.junit.Assert.*;
import static util.Utility.*;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

import util.Utility;

public class UtilityTest {

    @Test
    public void parseVersionNumberTest() {
        assertEquals(1, parseVersionNumber("1.2.3a").get()[0]);
        assertEquals(2, parseVersionNumber("1.2.3a").get()[1]);
        assertEquals(3, parseVersionNumber("1.2.3a").get()[2]);
    }

    @Test
    public void safeLongToIntTest() {
        long a = Integer.MAX_VALUE + 30L;
        try {
            safeLongToInt(a);
            fail();
        } catch (IllegalArgumentException ignored) {}

        a = (long) Integer.MIN_VALUE - 30L;
        try {
            safeLongToInt(a);
            fail();
        } catch (IllegalArgumentException ignored) {}
    }

    @Test
    public void stripQuotesTest() {
        assertEquals("test", stripQuotes("\"test\""));
    }

    @Test
    public void dateManipulationTest() {
        Calendar testDate = Calendar.getInstance();
        testDate.set(1994, Calendar.NOVEMBER, 15, 12, 45, 26);
        testDate.setTimeZone(TimeZone.getTimeZone("GMT"));

        assertEquals(testDate.getTime().toString(),
                parseHTTPLastModifiedDate("Tue, 15 Nov 1994 12:45:26 GMT").toString());

        assertEquals("1994-11-15T12:45Z", formatDateISO8601(testDate.getTime()));

        Date date = null;

        try {
            date = new SimpleDateFormat("yyyy-MM-dd-k-m-s").parse("1994-11-15-12-45-26");
        } catch (ParseException e) {
            fail();
        }

        assertEquals(date, localDateTimeToDate(LocalDateTime.of(1994, 11, 15, 12, 45, 26)));

        assertEquals(LocalDateTime.of(1994, 11, 15, 12, 45, 26), dateToLocalDateTime(date));

        assertEquals(date.getTime(), localDateTimeToLong(LocalDateTime.of(1994, 11, 15, 12, 45, 26)));
    }

    @Test
    public void snakeCaseTest() {
        assertEquals("HelloWorldThisIsATest", snakeCaseToCamelCase("hello_world_this_is_a_test"));
    }

    @Test(timeout = 5000)
    public void testGettingLookAndFeelOnLinux() {
        assertTrue(Utility.getUsableScreenDimensions().isPresent());
        assertTrue(Utility.getScreenDimensions() != null);
    }
}
