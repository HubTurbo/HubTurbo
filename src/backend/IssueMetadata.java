package backend;

import org.eclipse.egit.github.core.Comment;
import service.TurboIssueEvent;

import java.util.ArrayList;
import java.util.List;

public class IssueMetadata {
	private final List<TurboIssueEvent> events;
	private final List<Comment> comments;

	public IssueMetadata(List<TurboIssueEvent> events, List<Comment> comments) {
		this.events = events;
		this.comments = comments;
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
