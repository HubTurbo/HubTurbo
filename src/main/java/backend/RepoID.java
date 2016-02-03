package backend;

import org.eclipse.egit.github.core.RepositoryId;

/**
 * Represents the ID of a repository. To be used to ensure safe comparison
 */
public final class RepoID {

    private final String repoOwner;
    private final String repoName;

    /**
     * Constructor
     * @param repoIDString repoOwner and repoName separated by slash
     * @throws IllegalArgumentException If repoISString is invalid
     */
    public RepoID(String repoIDString) throws IllegalArgumentException {
        boolean isValidRepoID = RepoID.isWellFormedRepoId(repoIDString);
        if (isValidRepoID) {
            String[] repoIDComponents = repoIDString.toLowerCase().split("/");
            this.repoOwner = repoIDComponents[0];
            this.repoName = repoIDComponents[1];
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Constructor
     * @param repoOwner Owner of the repo
     * @param repoName Name of the repo
     * @throws IllegalArgumentException If repoOwner or repoName is invalid
     */
    public RepoID(String repoOwner, String repoName) throws IllegalArgumentException {
        boolean isValidRepoID = RepoID.isWellFormedRepoId(repoOwner, repoName);
        if (isValidRepoID) {
            this.repoOwner = repoOwner.toLowerCase();
            this.repoName = repoName.toLowerCase();
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static boolean isWellFormedRepoId(String owner, String repo) {
        return !(owner == null || owner.isEmpty() || repo == null || repo.isEmpty())
                && isWellFormedRepoId(RepositoryId.create(owner, repo).generateId());
    }

    public static boolean isWellFormedRepoId(String repoId) {
        RepositoryId repositoryId = RepositoryId.createFromId(repoId);
        return repoId != null && !repoId.isEmpty() && repositoryId != null
                && repoId.equals(repositoryId.generateId());
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
        RepoID repoID = (RepoID) o;
        if (!repoOwner.equals(repoID.repoOwner)) return false;
        return repoName.equals(repoID.repoName);
    }

    @Override
    public int hashCode() {
        int result = repoOwner.hashCode();
        result = 31 * result + repoName.hashCode();
        return result;
    }
}
