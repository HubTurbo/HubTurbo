package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import util.PlatformSpecific;
import util.PlatformSpecific.Architecture;
import browserview.BrowserComponent;

public class PlatformSpecificTests {

    @Test
    public void testStringToArchitectureEnum() {
        assertEquals(Architecture.X86_64,
                     PlatformSpecific.getArchitectureFromString("blah x86_64 blah"));
        assertEquals(Architecture.I386,
                     PlatformSpecific.getArchitectureFromString("blah i386 blah"));
        assertEquals(Architecture.I686,
                     PlatformSpecific.getArchitectureFromString("blah i686 blah"));
        assertEquals(Architecture.UNKNOWN,
                     PlatformSpecific.getArchitectureFromString("blah blah blah"));
        assertEquals(Architecture.UNKNOWN,
                     PlatformSpecific.getArchitectureFromString(""));
        assertEquals(Architecture.UNKNOWN,
                     PlatformSpecific.getArchitectureFromString(null));
    }

    @Test
    public void testGettingLinuxArchitecture() {
        if (PlatformSpecific.isOnLinux()) {
            assertTrue(PlatformSpecific.isOn32BitsLinux() || PlatformSpecific.isOn64BitsLinux());
        }

        if (!PlatformSpecific.isOnLinux()) {
            assertFalse(PlatformSpecific.isOn32BitsLinux());
            assertFalse(PlatformSpecific.isOn64BitsLinux());
        }

        if (PlatformSpecific.isOnMac()) {
            assertFalse(PlatformSpecific.isOn32BitsLinux());
            assertFalse(PlatformSpecific.isOn64BitsLinux());
        }

        if (PlatformSpecific.isOnWindows()) {
            assertFalse(PlatformSpecific.isOn32BitsLinux());
            assertFalse(PlatformSpecific.isOn64BitsLinux());
        }
    }

    @Test
    public void testMutuallyExclusiveOSName() {
        if (PlatformSpecific.isOnLinux()) {
            assertFalse(PlatformSpecific.isOnWindows());
            assertFalse(PlatformSpecific.isOnMac());
        }

        if (PlatformSpecific.isOnMac()) {
            assertFalse(PlatformSpecific.isOnWindows());
            assertFalse(PlatformSpecific.isOnLinux());
        }

        if (PlatformSpecific.isOnWindows()) {
            assertFalse(PlatformSpecific.isOnLinux());
            assertFalse(PlatformSpecific.isOnMac());
        }
    }

    @Test
    public void testDetermineChromeDriverBinaryName() {
        String chromeDriverBinaryName =
                BrowserComponent.determineChromeDriverBinaryName();

        assertNotNull(chromeDriverBinaryName);

        if (PlatformSpecific.isOnMac()) {
            assertTrue(chromeDriverBinaryName.startsWith("chromedriver_")
                    && !chromeDriverBinaryName.contains("exe")
                    && !chromeDriverBinaryName.contains("linux")
                    && !chromeDriverBinaryName.contains("x86_64"));
        } else if (PlatformSpecific.isOnWindows()) {
            assertTrue(chromeDriverBinaryName.startsWith("chromedriver_")
                    && chromeDriverBinaryName.contains("exe")
                    && !chromeDriverBinaryName.contains("linux")
                    && !chromeDriverBinaryName.contains("x86_64"));
        } else if (PlatformSpecific.isOn32BitsLinux()) {
            assertTrue(chromeDriverBinaryName.startsWith("chromedriver_")
                    && !chromeDriverBinaryName.contains("exe")
                    && chromeDriverBinaryName.contains("linux")
                    && !chromeDriverBinaryName.contains("x86_64"));
        } else if (PlatformSpecific.isOn64BitsLinux()) {
            assertTrue(chromeDriverBinaryName.startsWith("chromedriver_")
                    && !chromeDriverBinaryName.contains("exe")
                    && chromeDriverBinaryName.contains("linux")
                    && chromeDriverBinaryName.contains("x86_64"));
        } else {
            assertTrue(chromeDriverBinaryName.startsWith("chromedriver_")
                    && !chromeDriverBinaryName.contains("exe")
                    && chromeDriverBinaryName.contains("linux")
                    && !chromeDriverBinaryName.contains("x86_64"));
        }
    }

}
