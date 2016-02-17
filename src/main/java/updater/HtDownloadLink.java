package updater;

import util.Version;

import java.net.URL;

/**
 * Represents a download link to a HT version
 */
public class HtDownloadLink implements Comparable<HtDownloadLink> {
    private Version version;
    private URL downloadLinkUrl;

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public URL getDownloadLinkUrl() {
        return downloadLinkUrl;
    }

    public void setDownloadLinkUrl(URL downloadLinkUrl) {
        this.downloadLinkUrl = downloadLinkUrl;
    }

    @Override
    public int compareTo(HtDownloadLink other) {
        return this.version.compareTo(other.version);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof HtDownloadLink)) {
            return false;
        }
        final HtDownloadLink other = (HtDownloadLink) obj;

        return this.version.equals(other.version);
    }

    @Override
    public int hashCode() {
        return this.version.hashCode();
    }
}
