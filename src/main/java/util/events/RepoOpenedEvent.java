package util.events;

public class RepoOpenedEvent extends OpenReposChangedEvent {
    public final String repoId;
    public RepoOpenedEvent(String repoId) {
        this.repoId = repoId;
    }
}
