package backend;

import org.eclipse.egit.github.core.RepositoryId;
import util.Utility;

/**
 * Represents the ID of a repository. To be used to ensure safe comparison
 */
public final class RepoId {

    //repoOwner can be an org ID or username 
    private final String repoOwner;
    private final String repoName;

    public RepoId(String repoIdString) throws IllegalArgumentException {
        throwExceptionIfNotWellFormed(repoIdString);

        String[] repoIDComponents = repoIdString.toLowerCase().split("/");
        this.repoOwner = Utility.removeAllWhitespace(repoIDComponents[0]);
        this.repoName = Utility.removeAllWhitespace(repoIDComponents[1]);
    }

    /**
     * Returns true if the repoIdString is of the form
     * <repoOwner>/<repoName> e.g. dave/foo-project
     * @param repoIdString
     * @return
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
