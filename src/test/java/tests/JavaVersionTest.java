package tests;

import org.junit.Test;
import util.JavaVersion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class JavaVersionTest {
    @Test
    public void javaVersionParsing_Java8VersionNoBuild_ParsedVersionCorrectly() {
        String version = "1.8.0_60";
        JavaVersion expectedVersion = new JavaVersion(1, 8, 0, 60, 0);
        JavaVersion javaVersion = JavaVersion.fromString(version);
        assertEquals(expectedVersion, javaVersion);
    }

    @Test
    public void javaVersionParsing_Java8VersionWithBuild_ParsedVersionCorrectly() {
        String version = "1.8.0_60-b40";
        JavaVersion expectedVersion = new JavaVersion(1, 8, 0, 60, 40);
        JavaVersion javaVersion = JavaVersion.fromString(version);
        assertEquals(expectedVersion, javaVersion);
    }

    @Test
    public void javaVersionParsing_Java8VersionBigNumbers_ParsedVersionCorrectly() {
        String version = "10.80.100_60";
        JavaVersion expectedVersion = new JavaVersion(10, 80, 100, 60, 0);
        JavaVersion javaVersion = JavaVersion.fromString(version);
        assertEquals(expectedVersion, javaVersion);

        version = "010.080.100_60";
        expectedVersion = new JavaVersion(10, 80, 100, 60, 0);
        javaVersion = JavaVersion.fromString(version);
        assertEquals(expectedVersion, javaVersion);
    }

    @Test
    public void javaVersionParsing_Java8VersionNoUpdateNoBuild_ThrowsException() {
        String version = "1.8.0";
        try {
            JavaVersion.fromString(version);
            fail("Java version string with no update and built is parsed with no exception returned.");
        } catch (IllegalArgumentException e) {
            // pass
        }
    }

    @Test
    public void javaVersionParsing_NotJavaVersion_ThrowsException() {
        String version;

        version = "this should throw exception";
        try {
            JavaVersion.fromString(version);
            fail("Invalid Java version string is parsed with no exception returned.");
        } catch (IllegalArgumentException e) {
            // pass
        }

        version = "1.8_9";
        try {
            JavaVersion.fromString(version);
            fail("Invalid Java version string is parsed with no exception returned.");
        } catch (IllegalArgumentException e) {
            // pass
        }

        version = "1.0.7";
        try {
            JavaVersion.fromString(version);
            fail("Invalid Java version string is parsed with no exception returned.");
        } catch (IllegalArgumentException e) {
            // pass
        }
    }

    @Test
    public void javaVersionToString_JavaVersion_ReturnsCorrectString() {
        JavaVersion version = new JavaVersion(0, 0, 0, 0, 0);
        String versionString = "0.0.0_0-b0";

        assertEquals(versionString, version.toString());

        version = new JavaVersion(100, 200, 300, 400, 500);
        versionString = "100.200.300_400-b500";
        assertEquals(versionString, version.toString());
    }

    @Test
    public void javaVersionComparable_Java8Version_HashCodesAndEqualAreCorrect() {
        String version = "1.8.0_60";
        JavaVersion expectedVersion = new JavaVersion(1, 8, 0, 60, 0);
        JavaVersion javaVersion = JavaVersion.fromString(version);
        assertEquals(expectedVersion.hashCode(), javaVersion.hashCode());
        assertTrue(expectedVersion.equals(javaVersion));

        version = "9.8.0_60-b10";
        expectedVersion = new JavaVersion(9, 8, 0, 60, 10);
        javaVersion = JavaVersion.fromString(version);
        assertEquals(expectedVersion.hashCode(), javaVersion.hashCode());
        assertTrue(expectedVersion.equals(javaVersion));
    }

    @Test
    public void javaVersionTooLow_JavaVersion_ReturnsCorrectResult() {
        JavaVersion age0 = new JavaVersion(0, 0, 0, 0, 0);
        JavaVersion ageUpdate1 = new JavaVersion(0, 0, 0, 0, 1);
        JavaVersion ageBuild1Update0  = new JavaVersion(0, 0, 0, 1, 0);
        JavaVersion ageBuild1Update1  = new JavaVersion(0, 0, 0, 1, 1);
        JavaVersion ageMinor1 = new JavaVersion(0, 0, 1, 0, 0);
        JavaVersion ageMajor1 = new JavaVersion(0, 1, 0, 0, 0);
        JavaVersion ageDiscard2 = new JavaVersion(2, 0, 0, 0, 0);

        // Case version too low
        assertTrue(JavaVersion.isJavaVersionTooLow(age0, ageUpdate1));
        assertTrue(JavaVersion.isJavaVersionTooLow(ageUpdate1, ageBuild1Update0));
        assertTrue(JavaVersion.isJavaVersionTooLow(ageBuild1Update0, ageBuild1Update1));
        assertTrue(JavaVersion.isJavaVersionTooLow(ageBuild1Update1, ageMinor1));
        assertTrue(JavaVersion.isJavaVersionTooLow(ageBuild1Update1, ageMajor1));
        assertTrue(JavaVersion.isJavaVersionTooLow(ageMinor1, ageMajor1));
        assertTrue(JavaVersion.isJavaVersionTooLow(ageMajor1, ageDiscard2));

        // Case equal
        assertFalse(JavaVersion.isJavaVersionTooLow(age0, age0));
        assertFalse(JavaVersion.isJavaVersionTooLow(ageUpdate1, ageUpdate1));

        // Case version higher
        assertFalse(JavaVersion.isJavaVersionTooLow(ageBuild1Update0, ageUpdate1));
        assertFalse(JavaVersion.isJavaVersionTooLow(ageDiscard2, ageMajor1));
    }
}
