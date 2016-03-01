package backend;

import org.eclipse.egit.github.core.RepositoryId;
import util.Utility;

/**
 * RepoId represents the ID of a repository. It is to be used to ensure safe comparison.
 * All the String attributes of the class is kept in lower case.
 */
public final class RepoId {

    // repoOwner can be an organisation ID or username
    private final String repoOwner;
    private final String repoName;

    public RepoId(String repoId) {
        throwExceptionIfNotWellFormed(repoId);

        String[] repoIDComponents = repoId.toLowerCase().split("/");
        this.repoOwner = Utility.removeAllWhitespace(repoIDComponents[0]);
        this.repoName = Utility.removeAllWhitespace(repoIDComponents[1]);
    }

    /**
     * Delegates to EGit's RepositoryId to check if repoString is
     * of the correct format
     *
     * @param repoId
     * @return true if the repoId is of the form
     * <repoOwner>/<repoName> e.g. dave/foo-project
     */
    public static boolean isValidRepoId(String repoId) {
        RepositoryId idGeneratedByEgit = RepositoryId.createFromId(repoId);
        return idGeneratedByEgit != null && repoId.equals(idGeneratedByEgit.generateId());
    }

    public String getRepoOwner() {
        return repoOwner;
    }

    public String getRepoName() {
        return repoName;
    }

    @Override
    public String toString() {
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
        boolean isValidRepoId = RepoId.isValidRepoId(repoIdString);
        if (!isValidRepoId) {
            throw new IllegalArgumentException();
        }
    }
}
