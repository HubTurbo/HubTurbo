package backend;

public class SerializableIssue {
	public final int id;
	public final String title;

	public SerializableIssue(TurboIssue issue) {
		this.id = issue.getId();
		this.title = issue.getTitle();
	}
}
