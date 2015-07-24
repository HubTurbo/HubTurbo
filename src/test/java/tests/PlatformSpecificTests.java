package tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import util.PlatformSpecific;
import util.PlatformSpecific.Architecture;

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

}
