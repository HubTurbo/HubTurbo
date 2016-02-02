package util.events;

/**
 * The RepoOpenedEvent is meant to indicate that a Repo has been opened
 *
 * It contains the ID of the repo that has been opened, along with the indication of whether
 * it is the primary repo.
 */
public class RepoOpenedEvent extends UnusedStoredReposChangedEvent {
    public final String repoId;
    public final boolean isPrimaryRepo;

    public RepoOpenedEvent(String repoId, boolean isPrimaryRepo) {
        this.repoId = repoId;
        this.isPrimaryRepo = isPrimaryRepo;
    }
}
