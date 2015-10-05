package util.events;

public class RepoOpenedEvent extends UnusedStoredReposChangedEvent {
    public final String repoId;
    public RepoOpenedEvent(String repoId) {
        this.repoId = repoId;
    }
}
