package backend;

import github.TurboIssueEvent;
import org.eclipse.egit.github.core.Comment;

import java.util.ArrayList;
import java.util.List;

public class IssueMetadata {
	private final List<TurboIssueEvent> events;
	private final List<Comment> comments;

	public IssueMetadata() {
		events = new ArrayList<>();
		comments = new ArrayList<>();
	}

	public IssueMetadata(List<TurboIssueEvent> events, List<Comment> comments) {
		this.events = events;
		this.comments = comments;
	}

	public IssueMetadata(IssueMetadata other) {
		this.events = new ArrayList<>(other.events);
		this.comments = new ArrayList<>(other.comments);
	}

	public String summarise() {
		return String.format("%d events, %d comments", events.size(), comments.size());
	}

	public List<TurboIssueEvent> getEvents() {
		return new ArrayList<>(events);
	}

	public List<Comment> getComments() {
		return new ArrayList<>(comments);
	}

	@Override
	public String toString() {
		return "Events: " + events.toString() + ", " + "comments: " + comments.toString();
	}
}
