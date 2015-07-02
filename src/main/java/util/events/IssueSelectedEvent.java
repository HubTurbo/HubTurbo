package util.events;

public class IssueSelectedEvent extends Event {
    public final String repoId;
    public final int id;
    public final int panelIndex;
    public final boolean isPullRequest;

    public IssueSelectedEvent(String repoId, int id, int panelIndex, boolean isPullRequest) {
        this.repoId = repoId;
        this.id = id;
        this.panelIndex = panelIndex;
        this.isPullRequest = isPullRequest;
    }
}
