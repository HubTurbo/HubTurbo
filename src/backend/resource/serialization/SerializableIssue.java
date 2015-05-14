package backend.resource.serialization;

import backend.resource.TurboIssue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class SerializableIssue {
	public final int id;
	public final String title;
	public final String creator;
	public final LocalDateTime createdAt;
	public final boolean isPullRequest;
	public final String description;
	public final LocalDateTime updatedAt;
	public final int commentCount;
	public final boolean isOpen;
	public final Optional<String> assignee;
	public final List<String> labels;
	public final Optional<Integer> milestone;

	public SerializableIssue(TurboIssue issue) {
		this.id = issue.getId();
		this.title = issue.getTitle();
		this.creator = issue.getCreator();
		this.createdAt = issue.getCreatedAt();
		this.isPullRequest = issue.isPullRequest();
		this.description = issue.getDescription();
		this.updatedAt = issue.getUpdatedAt();
		this.commentCount = issue.getCommentCount();
		this.isOpen = issue.isOpen();
		this.assignee = issue.getAssignee();
		this.labels = issue.getLabels();
		this.milestone = issue.getMilestone();
	}
}
