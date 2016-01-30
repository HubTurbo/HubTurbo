package backend;

import util.Utility;

import java.util.Optional;

/**
 * Represents the ID of a repository. To be used to ensure safe comparison
 */
public final class RepoID {

    private final String repoIDString;
    private final String repoOwner;
    private final String repoName;

    /**
     * Return an Optional with a new instance of repoID if repoIDString is valid
     * @param repoIDString
     * @return Optional<RepoID>
     */
    public static Optional<RepoID> createRepoIDInstance(String repoIDString){
        String lowerCaseRepoIDString = repoIDString.toLowerCase();
        boolean isValidRepoIDString = Utility.isWellFormedRepoId(repoIDString);
        if (isValidRepoIDString) {
            String[] repoIDComponents = lowerCaseRepoIDString.split("/");
            RepoID repoID = new RepoID(lowerCaseRepoIDString, repoIDComponents[0], repoIDComponents[1]);
            return Optional.of(repoID);
        } else {
            return Optional.empty();
        }
    }

    public String getRepoOwner() {
        return repoOwner;
    }

    public String getRepoName() {
        return repoName;
    }

    public String getRepoIDString() {
        return repoIDString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepoID repoID = (RepoID) o;
        if (!repoIDString.equals(repoID.repoIDString)) return false;
        if (!repoOwner.equals(repoID.repoOwner)) return false;
        return repoName.equals(repoID.repoName);
    }

    @Override
    public int hashCode() {
        int result = repoIDString.hashCode();
        result = 31 * result + repoOwner.hashCode();
        result = 31 * result + repoName.hashCode();
        return result;
    }

    private RepoID(String lowerCaseRepoIDString, String repoOwner, String repoName){
        this.repoIDString = lowerCaseRepoIDString;
        this.repoOwner = repoOwner;
        this.repoName = repoName;
    }
}
