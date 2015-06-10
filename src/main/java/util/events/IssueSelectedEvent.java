package util.events;

public class IssueSelectedEvent extends Event {
	public final String repoId;
	public final int id;
	public final int columnIndex;
	
	public IssueSelectedEvent(String repoId, int id, int columnIndex) {
		this.repoId = repoId;
		this.id = id;
		this.columnIndex = columnIndex;
	}
}
