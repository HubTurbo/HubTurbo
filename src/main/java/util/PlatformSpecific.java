package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.logging.log4j.Logger;

public class PlatformSpecific {
    private static final Logger logger = HTLog.get(PlatformSpecific.class);

//    private static final PlatformSpecific instance = new PlatformSpecific();
    private PlatformSpecific() {}
    private static final String osName = System.getProperty("os.name");

    public static enum Architecture {
        UNKNOWN, X86_64, I386, I686
    }

    public static boolean isOnWindows() {
        return osName.startsWith("Windows");
    }

    public static boolean isOnMac() {
        return osName.startsWith("Mac OS");
    }

    public static boolean isOn64BitsLinux() {
        if (!isOnLinux()) {
            return false;
        }

        return getLinuxKernelArchitecture() == Architecture.X86_64;
    }

    public static boolean isOn32BitsLinux() {
        if (!isOnLinux()) {
            return false;
        }

        Architecture architecture = getLinuxKernelArchitecture();

        return architecture == Architecture.I386 ||
                architecture == Architecture.I686;
    }

    public static boolean isOnLinux() {
        return osName.startsWith("Linux");
    }

    private static Architecture getLinuxKernelArchitecture() {
        try {
            Process process = Runtime.getRuntime().exec("uname -m");
            process.waitFor();

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));

            String output = "";
            String nextOutputLine = reader.readLine();

            while (nextOutputLine != null) {
                output += nextOutputLine + " ";
                nextOutputLine = reader.readLine();
            }

            return getArchitectureFromString(output);
        } catch (IOException e) {
            logger.error("Unable to get linux kernel architecture");
            logger.error(e.getLocalizedMessage());

            return Architecture.UNKNOWN;
        } catch (InterruptedException e) {
            logger.error("Unable to get linux kernel architecture");
            logger.error(e.getLocalizedMessage());

            return Architecture.UNKNOWN;
        }
    }

    /**
     * Finds a relevant sub-string that characterizes an os architecture and
     * return the corresponding Architecture enum
     * @param architectureDescription a string description of an os architecture
     * @return the corresponding Architecture enum
     */
    public static Architecture getArchitectureFromString(String architectureDescription) {
        if (architectureDescription == null) {
            return Architecture.UNKNOWN;
        } else if (architectureDescription.contains("x86_64")) {
            return Architecture.X86_64;
        } else if (architectureDescription.contains("i386")) {
            return Architecture.I386;
        } else if (architectureDescription.contains("i686")) {
            return Architecture.I686;
        } else {
            return Architecture.UNKNOWN;
        }
    }

//    public static PlatformSpecific ifWindows(Runnable code) {
//        if (isOnWindows()) {
//            code.run();
//        }
//        return instance;
//    }

//    public static PlatformSpecific ifMac(Runnable code) {
//        if (isOnMac()) {
//            code.run();
//        }
//        return instance;
//    }

//    public static PlatformSpecific ifLinux(Runnable code) {
//        if (isOnLinux()) {
//            code.run();
//        }
//        return instance;
//    }
}
