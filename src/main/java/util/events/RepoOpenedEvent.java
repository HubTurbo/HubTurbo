package util.events;

public class RepoOpenedEvent extends Event {
	public final String repoId;
	public RepoOpenedEvent(String repoId) {
		this.repoId = repoId;
	}
}
