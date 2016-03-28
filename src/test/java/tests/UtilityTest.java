package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import java.util.*;

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
        } catch (IllegalArgumentException ignored) {
        }

        a = Integer.MIN_VALUE - 30L;
        try {
            safeLongToInt(a);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
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
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void dateManipulationTest() {
        Calendar testDate = Calendar.getInstance();
        testDate.set(1994, Calendar.NOVEMBER, 15, 12, 45, 26);
        testDate.setTimeZone(TimeZone.getTimeZone("GMT"));

        assertEquals("1994-11-15T12:45Z", formatDateISO8601(testDate.getTime()));

        Date date = null;

        try {
            date = new SimpleDateFormat("yyyy-MM-dd-k-m-s", Locale.US).parse("1994-11-15-12-45-26");
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

    @Test
    public void generateNameTest() {
        // append index '1' or greater to desired name,
        // whichever smallest index is available

        String desiredName = "test";

        assertEquals(desiredName, Utility.getNameClosestToDesiredName(desiredName, new ArrayList<String>()));
        assertEquals(desiredName, Utility.getNameClosestToDesiredName(desiredName,
                                                                      Arrays.asList("not-test", "other", "even more")));
        assertEquals(desiredName + "1", Utility.getNameClosestToDesiredName(desiredName,
                                                                            Arrays.asList("test")));
        assertEquals(desiredName, Utility.getNameClosestToDesiredName(desiredName,
                                                                      Arrays.asList("test1")));
        assertEquals(desiredName + "2", Utility.getNameClosestToDesiredName(desiredName,
                                                                            Arrays.asList("test", "test1")));
        assertEquals(desiredName + "1", Utility.getNameClosestToDesiredName(desiredName,
                                                                            Arrays.asList("test", "test2")));
        assertEquals(desiredName, Utility.getNameClosestToDesiredName(desiredName,
                                                                      Arrays.asList("tests")));
        assertEquals(desiredName + "1", Utility.getNameClosestToDesiredName(desiredName,
                                                                            Arrays.asList("test", "tests")));
        assertEquals(desiredName + "1", Utility.getNameClosestToDesiredName(desiredName,
                                                                            Arrays.asList("test", "test100")));
    }

    @Test
    public void convertSetToLowerCaseTest() {
        final String entry1 = "Test1";
        final String entry2 = "tesT2";
        final String entry3 = "correct";
        final String entry4 = "WRONG";
        final String entry5 = "wiTH/slaSH";

        Set<String> testSet = new HashSet<>(Arrays.asList(
                entry1, entry2, entry3, entry4, entry5
        ));

        Set<String> convertedSet = Utility.convertSetToLowerCase(testSet);

        assertEquals(false, convertedSet.contains(entry1));
        assertEquals(false, convertedSet.contains(entry2));
        assertEquals(true, convertedSet.contains(entry3));
        assertEquals(false, convertedSet.contains(entry4));
        assertEquals(false, convertedSet.contains(entry5));

        assertEquals(true, convertedSet.contains(entry1.toLowerCase()));
        assertEquals(true, convertedSet.contains(entry2.toLowerCase()));
        assertEquals(true, convertedSet.contains(entry3.toLowerCase()));
        assertEquals(true, convertedSet.contains(entry4.toLowerCase()));
        assertEquals(true, convertedSet.contains(entry5.toLowerCase()));


    }

    @Test
    public void containsIgnoreCaseMultipleWords_partialMatchingQueries() {
        assertFalse(Utility.containsIgnoreCase("this is", Arrays.asList("is", "me")));
    }

    @Test
    public void containsIgnoreCaseMultipleWords_allMatchingQueries() {
        assertTrue(Utility.containsIgnoreCase("this is me", Arrays.asList("is", "me")));
    }
}
