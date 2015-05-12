package backend;

public class SerializableIssue {
	public final String title;

	public SerializableIssue(TurboIssue issue) {
		this.title = issue.getTitle();
	}
}
