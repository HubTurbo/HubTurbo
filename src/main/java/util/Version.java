package util;

import ui.UI;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a version with major, minor and patch number
 */
public class Version implements Comparable<Version> {

    public static final String VERSION_PATTERN_STRING = "V(\\d+)\\.(\\d+)\\.(\\d+)";

    private static final String EXCEPTION_STRING_NOT_VERSION = "String is not a valid Version. %s";

    private static final Pattern VERSION_PATTERN = Pattern.compile(VERSION_PATTERN_STRING);

    private final int major;
    private final int minor;
    private final int patch;

    public Version(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    /**
     * Parses a version number string in the format V1.2.3.
     * @param versionString version number string
     * @return a Version object
     */
    public static Version fromString(String versionString) throws IllegalArgumentException {
        Matcher versionMatcher = VERSION_PATTERN.matcher(versionString);

        if (!versionMatcher.find()) {
            throw new IllegalArgumentException(String.format(EXCEPTION_STRING_NOT_VERSION, versionString));
        }

        return new Version(Integer.parseInt(versionMatcher.group(1)),
                           Integer.parseInt(versionMatcher.group(2)),
                           Integer.parseInt(versionMatcher.group(3)));
    }

    /**
     * Checks if a given version is of same major or just one major greater than current version
     * @param version
     */
    public static boolean isVersionMajorSameOrJustOneGreaterFromCurrent(Version version) {
        return version.getMajor() == getCurrentVersion().getMajor() ||
                version.getMajor() == getCurrentVersion().getMajor() + 1;
    }

    public String toString() {
        return String.format("V%d.%d.%d", major, minor, patch);
    }

    /**
     * Gets HubTurbo current version
     * @return version object of HubTurbo's current version
     */
    public static Version getCurrentVersion() {
        return new Version(UI.VERSION_MAJOR, UI.VERSION_MINOR, UI.VERSION_PATCH);
    }

    @Override
    public int compareTo(Version other) {
        return this.major != other.major ? this.major - other.major :
                this.minor != other.minor ? this.minor - other.minor :
                this.patch != other.patch ? this.patch - other.patch : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Version)) {
            return false;
        }
        final Version other = (Version) obj;

        return this.compareTo(other) == 0;
    }

    @Override
    public int hashCode() {
        String hash = String.format("%03d%03d%03d", major, minor, patch);
        return Integer.parseInt(hash);
    }
}
