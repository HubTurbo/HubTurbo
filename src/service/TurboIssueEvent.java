package service;

import java.util.Date;

import org.eclipse.egit.github.core.User;
import org.ocpsoft.prettytime.PrettyTime;

/**
 * Models an event that could happen to an issue.
 */
public class TurboIssueEvent {
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

	@Override
	public String toString() {
		String actorName = getActor().getLogin();
		String time = new PrettyTime().format(getDate());
		String message;

		switch (getType()) {
			case Renamed:
				message = String.format("%s renamed this issue %s.", actorName, time);
				break;
			case Milestoned:
				message = String.format("%s added milestone %s %s.", actorName, getMilestoneTitle(), time);
				break;
			case Demilestoned:
				message = String.format("%s removed milestone %s %s.", actorName, getMilestoneTitle(), time);
				break;
			case Labeled:
				message = String.format("%s added label %s %s.", actorName, getLabelName(), time);
				break;
			case Unlabeled:
				message = String.format("%s removed label %s %s.", actorName, getLabelName(), time);
				break;
			case Assigned:
				message = String.format("%s was assigned to this issue %s.", actorName, time);
				break;
			case Unassigned:
				message = String.format("%s was unassigned from this issue %s.", actorName, time);
				break;
			case Closed:
				message = String.format("%s closed this issue %s.", actorName, time);
				break;
			case Reopened:
				message = String.format("%s reopened this issue %s.", actorName, time);
				break;
			case Locked:
				message = String.format("%s locked issue %s.", actorName, time);
				break;
			case Unlocked:
				message = String.format("%s unlocked this issue %s.", actorName, time);
				break;
			case Referenced:
				message = String.format("%s referenced this issue %s.", actorName, time);
				break;
			case Subscribed:
				message = String.format("%s subscribed to receive notifications for this issue %s.", actorName, time);
				break;
			case Mentioned:
				message = String.format("%s was mentioned %s.", actorName, time);
				break;
			case Merged:
				message = String.format("%s merged this issue %s.", actorName, time);
				break;
			case HeadRefDeleted:
				message = String.format("%s deleted the pull request's branch %s.", actorName, time);
				break;
			case HeadRefRestored:
				message = String.format("%s restored the pull request's branch %s.", actorName, time);
				break;
			default:
				// Not yet implemented, or no events triggered
				message = String.format("%s %s %s.", actorName, getType(), time);
				break;
		}
		return message;
	}
}
