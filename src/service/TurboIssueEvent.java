package service;

import java.util.Date;

import org.eclipse.egit.github.core.User;

import ui.issuepanel.expanded.comments.CommentListItem;

/**
 * Models an event that could happen to an issue.
 */
public class TurboIssueEvent implements CommentListItem {
	private Date date;
	private IssueEventType type;
	private User actor;
	private String labelName, labelColour;
	private String milestoneTitle;
	private String renamedFrom, renamedTo;
	private User assignedUser;

	public TurboIssueEvent(User actor, IssueEventType type, Date date) {
		this.type = type;
		this.actor = actor;
		this.date = date;
	}
	public IssueEventType getType() {
		return type;
	}
	public User getActor() {
		return actor;
	}
	public Date getDate() {
		return date;
	}
	
	public String getLabelName() {
		assert type == IssueEventType.Labeled || type == IssueEventType.Unlabeled;
		return labelName;
	}
	public void setLabelName(String labelName) {
		assert type == IssueEventType.Labeled || type == IssueEventType.Unlabeled;
		this.labelName = labelName;
	}
	public String getLabelColour() {
		assert type == IssueEventType.Labeled || type == IssueEventType.Unlabeled;
		return labelColour;
	}
	public void setLabelColour(String labelColour) {
		assert type == IssueEventType.Labeled || type == IssueEventType.Unlabeled;
		this.labelColour = labelColour;
	}
	public String getMilestoneTitle() {
		assert type == IssueEventType.Milestoned || type == IssueEventType.Demilestoned;
		return milestoneTitle;
	}
	public void setMilestoneTitle(String milestoneTitle) {
		assert type == IssueEventType.Milestoned || type == IssueEventType.Demilestoned;
		this.milestoneTitle = milestoneTitle;
	}
	public String getRenamedFrom() {
		assert type == IssueEventType.Renamed || type == IssueEventType.Renamed;
		return renamedFrom;
	}
	public void setRenamedFrom(String renamedFrom) {
		assert type == IssueEventType.Renamed || type == IssueEventType.Renamed;
		this.renamedFrom = renamedFrom;
	}
	public String getRenamedTo() {
		assert type == IssueEventType.Renamed || type == IssueEventType.Renamed;
		return renamedTo;
	}
	public void setRenamedTo(String renamedTo) {
		assert type == IssueEventType.Renamed || type == IssueEventType.Renamed;
		this.renamedTo = renamedTo;
	}
	public User getAssignedUser() {
		assert type == IssueEventType.Assigned || type == IssueEventType.Unassigned;
		return assignedUser;
	}
	public void setAssignedUser(User assignedUser) {
		assert type == IssueEventType.Assigned || type == IssueEventType.Unassigned;
		this.assignedUser = assignedUser;
	}
}
