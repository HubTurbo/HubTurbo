package backend;

import org.eclipse.egit.github.core.RepositoryId;
import util.Utility;

/**
 * Represents the ID of a repository. To be used to ensure safe comparison
 */
public final class RepoId {

    private final String repoOwner;
    private final String repoName;

    public RepoId(String repoIdString) throws IllegalArgumentException {
        throwExceptionIfNotWellFormed(repoIdString);

        String[] repoIDComponents = repoIdString.toLowerCase().split("/");
        this.repoOwner = Utility.removeAllWhitespace(repoIDComponents[0]);
        this.repoName = Utility.removeAllWhitespace(repoIDComponents[1]);
    }

    /**
     * Checks if repoIdString non-null, non-empty
     * and is of form <repoOwner>/<repoName>
     * @param repoIdString
     * @return True if the above conditions are met else False is returned
     */
    public static boolean isWellFormedRepoIdString(String repoIdString) {
        RepositoryId repositoryId = RepositoryId.createFromId(repoIdString);
        return repoIdString != null && !repoIdString.isEmpty() && repositoryId != null
                && repoIdString.equals(repositoryId.generateId());
    }

    public String getRepoOwner() {
        return repoOwner;
    }

    public String getRepoName() {
        return repoName;
    }

    public String getRepoIDString() {
        return repoOwner + "/" + repoName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepoId repoId = (RepoId) o;
        if (!repoOwner.equals(repoId.repoOwner)) return false;
        return repoName.equals(repoId.repoName);
    }

    @Override
    public int hashCode() {
        int result = repoOwner.hashCode();
        result = 31 * result + repoName.hashCode();
        return result;
    }

    private void throwExceptionIfNotWellFormed(String repoIdString) {
        boolean isWellFormed = RepoId.isWellFormedRepoIdString(repoIdString);
        if (!isWellFormed) {
            throw new IllegalArgumentException();
        }
    }
}
