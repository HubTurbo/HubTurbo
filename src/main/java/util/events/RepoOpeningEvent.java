package util.events;

/**
 * The RepoOpeningEvent is meant to indicate that a Repo is currently opening
 *
 * It contains the ID of the repo that is currently opening, along with the indication of whether
 * it is the primary repo.
 */
public class RepoOpeningEvent extends Event {

    public final String repoId;
    public final boolean isPrimaryRepo;

    public RepoOpeningEvent(String repoId, boolean isPrimaryRepo) {
        this.repoId = repoId;
        this.isPrimaryRepo = isPrimaryRepo;
    }

}
