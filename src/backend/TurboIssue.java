package backend;

import org.eclipse.egit.github.core.Issue;

public class TurboIssue {
	private String title;
	private final int id;

	public TurboIssue(int id, String title) {
		this.id = id;
		this.title = title;
	}

	public TurboIssue(Issue issue) {
		this.id = issue.getNumber();
		this.title = issue.getTitle();
	}

	public TurboIssue(SerializableIssue issue) {
		this.title = issue.title;
		this.id = issue.id;
	}

	public int getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	@Override
	public String toString() {
		return "#" + id + " " + title;
	}
}
