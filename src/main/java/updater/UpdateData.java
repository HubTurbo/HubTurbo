package updater;

import util.Version;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represents update data to be put on server for HT to check if there is new update
 */
public class UpdateData {

    private List<HtDownloadLink> downloadLinks = new ArrayList<>(); // NOPMD - not made final for gson

    /**
     * Gets HtDownloadLink for HT to update to.
     * @return HtDownloadLink that current version can update to, Optional.empty() otherwise
     */
    public Optional<HtDownloadLink> getLatestUpdateDownloadLinkForCurrentVersion() {
        if (downloadLinks.isEmpty()) {
            return Optional.empty();
        }

        // List the update link in descending order of version
        Collections.sort(downloadLinks, Collections.reverseOrder());

        // Get link of version that has same major version or just 1 major version up than current
        return downloadLinks.stream()
                .filter(link -> Version.isVersionMajorSameOrJustOneGreaterFromCurrent(link.getVersion())).findFirst();
    }
}
