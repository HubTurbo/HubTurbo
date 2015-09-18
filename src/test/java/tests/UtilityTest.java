package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static util.Utility.dateToLocalDateTime;
import static util.Utility.formatDateISO8601;
import static util.Utility.localDateTimeToDate;
import static util.Utility.localDateTimeToLong;
import static util.Utility.parseHTTPLastModifiedDate;
import static util.Utility.parseVersionNumber;
import static util.Utility.replaceNull;
import static util.Utility.safeLongToInt;
import static util.Utility.snakeCaseToCamelCase;
import static util.Utility.stripQuotes;

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

        a = Integer.MIN_VALUE - 30L;
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
    public void parseHTTPLastModifiedDateTest() {
        Calendar testDate = Calendar.getInstance();
        testDate.set(1994, Calendar.NOVEMBER, 15, 12, 45, 26);
        testDate.setTimeZone(TimeZone.getTimeZone("GMT"));

        assertEquals(testDate.getTime().toString(),
            parseHTTPLastModifiedDate("Tue, 15 Nov 1994 12:45:26 GMT").toString());

        try {
            assertEquals(testDate.getTime().toString(),
                parseHTTPLastModifiedDate("some non-date string").toString());
            fail();
        } catch (IllegalArgumentException ignored) {}
    }

    @Test
    public void dateManipulationTest() {
        Calendar testDate = Calendar.getInstance();
        testDate.set(1994, Calendar.NOVEMBER, 15, 12, 45, 26);
        testDate.setTimeZone(TimeZone.getTimeZone("GMT"));

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

    @Test
    public void ignoreCaseTest() {
        assertTrue(Utility.containsIgnoreCase("HeLlO wOrLd", "lLo"));
        assertTrue(Utility.containsIgnoreCase("hello world", "O W"));
        assertTrue(Utility.containsIgnoreCase("!@#$%test^&*()", "$%TeSt^&"));
        assertTrue(Utility.containsIgnoreCase("1A2B3C", "a2b3"));
        assertTrue(Utility.startsWithIgnoreCase("HeLlO wOrLd", "hello"));
        assertTrue(Utility.startsWithIgnoreCase("HeLlO wOrLd", "hElLo"));
        assertTrue(Utility.startsWithIgnoreCase("!@#$%test^&*()", "!@#$%Te"));
        assertTrue(Utility.startsWithIgnoreCase("1A2B3C", "1a2B"));
    }

    @Test
    public void testReplaceNull() {
        assertEquals("123", replaceNull("123", ""));
        assertEquals("", replaceNull(null, ""));

        Object obj = new Object();
        assertEquals(obj, replaceNull(obj, new Object()));
        assertEquals(obj, replaceNull(null, obj));
    }
}
