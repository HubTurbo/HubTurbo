package backend;

import org.eclipse.egit.github.core.Issue;

public class TurboIssue {
	private String title;

	public TurboIssue(String title) {
		this.title = title;
	}

	public TurboIssue(Issue issue) {
		this.title = "#" + issue.getNumber() + " " + issue.getTitle();
	}

	public TurboIssue(SerializableIssue issue) {
		this.title = issue.title;
	}

	public String getTitle() {
		return title;
	}

	@Override
	public String toString() {
		return title;
	}
}
