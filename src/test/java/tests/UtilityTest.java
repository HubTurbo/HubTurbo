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
    public void utilityTest() {
        assertEquals("test", stripQuotes("\"test\""));

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

        assertEquals("HelloWorldThisIsATest", snakeCaseToCamelCase("hello_world_this_is_a_test"));

        assertEquals(1, parseVersionNumber("1.2.3a").get()[0]);
        assertEquals(2, parseVersionNumber("1.2.3a").get()[1]);
        assertEquals(3, parseVersionNumber("1.2.3a").get()[2]);
    }

    @Test(timeout = 5000)
    public void testGettingLookAndFeelOnLinux() {
        assertTrue(Utility.getUsableScreenDimensions().isPresent());
        assertTrue(Utility.getScreenDimensions() != null);
    }
}
