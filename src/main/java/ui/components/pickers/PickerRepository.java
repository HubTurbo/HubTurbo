package ui.components.pickers;

/**
 * A class used to store a repo with a capability of deciding whether
 * a particular query suits this repo or not based on the mode of
 * matching.
 */
public class PickerRepository {

    private final String repositoryId;

    public PickerRepository(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public boolean isMatching(String query, MatchingMode matchingMode) {
        if (matchingMode == MatchingMode.PREFIX_MATCHING) {
            return repositoryId.startsWith(query);
        } else if (matchingMode == MatchingMode.SUBSTRING_MATCHING) {
            return repositoryId.contains(query);
        }
        assert false : "MatchingMode is not supported.";
        return false;
    }
}
