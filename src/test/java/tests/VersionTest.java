package tests;

import org.junit.Test;
import ui.UI;
import util.Version;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VersionTest {
    @Test
    public void versionGetCurrent_CompareWithUiValue_VersionEqual() {
        Version currentVersion = Version.getCurrentVersion();
        assertEquals(UI.VERSION_MAJOR, currentVersion.getMajor());
        assertEquals(UI.VERSION_MINOR, currentVersion.getMinor());
        assertEquals(UI.VERSION_PATCH, currentVersion.getPatch());
        assertEquals(new Version(UI.VERSION_MAJOR, UI.VERSION_MINOR, UI.VERSION_PATCH), currentVersion);
    }

    @Test
    public void versionParsing_AcceptableVersionString_ParsedVersionCorrectly() {
        verifyVersionParsedCorrectly("V0.0.0", 0, 0, 0);
        verifyVersionParsedCorrectly("V3.10.2", 3, 10, 2);
        verifyVersionParsedCorrectly("V100.100.100", 100, 100, 100);
    }

    @Test
    public void versionConstructor_CorrectParameter_ValueAsExpected() {
        Version version = new Version(19, 10, 20);

        assertEquals(19, version.getMajor());
        assertEquals(10, version.getMinor());
        assertEquals(20, version.getPatch());
    }

    @Test
    public void versionToString_ValidVersion_CorrectStringRepresentation() {
        // boundary at 0
        Version version = new Version(0, 0, 0);
        assertEquals("V0.0.0", version.toString());

        // normal values
        version = new Version(4, 10, 5);
        assertEquals("V4.10.5", version.toString());

        // big numbers
        version = new Version(100, 100, 100);
        assertEquals("V100.100.100", version.toString());
    }

    @Test
    public void versionComparable_ValidVersion_CompareToIsCorrect() {
        Version one, another;

        // Tests equality
        one = new Version(0, 0, 0);
        another = new  Version(0, 0, 0);
        assertTrue(one.compareTo(another) == 0);

        one = new Version(11, 12, 13);
        another = new  Version(11, 12, 13);
        assertTrue(one.compareTo(another) == 0);

        // Tests different patch
        one = new Version(0, 0, 5);
        another = new  Version(0, 0, 0);
        assertTrue(one.compareTo(another) > 0);

        // Tests different minor
        one = new Version(0, 0, 0);
        another = new  Version(0, 5, 0);
        assertTrue(one.compareTo(another) < 0);

        // Tests different major
        one = new Version(10, 0, 0);
        another = new  Version(0, 0, 0);
        assertTrue(one.compareTo(another) > 0);

        // Tests high major vs low minor
        one = new Version(10, 0, 0);
        another = new  Version(0, 1, 0);
        assertTrue(one.compareTo(another) > 0);

        // Tests high patch vs low minor
        one = new Version(0, 0, 10);
        another = new  Version(0, 1, 0);
        assertTrue(one.compareTo(another) < 0);

        // Tests same major minor different patch
        one = new Version(2, 15, 0);
        another = new  Version(2, 15, 5);
        assertTrue(one.compareTo(another) < 0);
    }

    @Test
    public void versionComparable_ValidVersion_HashCodeIsCorrect() {
        Version version = new Version(100, 100, 100);
        assertEquals(100100100, version.hashCode());
    }

    @Test
    public void versionComparable_ValidVersion_EqualIsCorrect() {
        Version one, another;

        one = new Version(0, 0, 0);
        another = new  Version(0, 0, 0);
        assertTrue(one.equals(another));

        one = new Version(100, 191, 275);
        another = new  Version(100, 191, 275);
        assertTrue(one.equals(another));
    }

    private void verifyVersionParsedCorrectly(String versionString, int major, int minor, int patch) {
        assertEquals(new Version(major, minor, patch), Version.fromString(versionString));
    }
}
