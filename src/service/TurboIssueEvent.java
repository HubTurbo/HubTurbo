package service;

import java.util.Date;

import javafx.scene.Node;
import javafx.scene.text.Text;
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

	public Node display() {
		String actorName = getActor().getLogin();
		String time = new PrettyTime().format(getDate());

		switch (getType()) {
			case Renamed:
				return new Text(String.format("%s renamed this issue %s.", actorName, time));
			case Milestoned:
				return new Text(String.format("%s added milestone %s %s.", actorName, getMilestoneTitle(), time));
			case Demilestoned:
				return new Text(String.format("%s removed milestone %s %s.", actorName, getMilestoneTitle(), time));
			case Labeled:
				return new Text(String.format("%s added label %s %s.", actorName, getLabelName(), time));
			case Unlabeled:
				return new Text(String.format("%s removed label %s %s.", actorName, getLabelName(), time));
			case Assigned:
				return new Text(String.format("%s was assigned to this issue %s.", actorName, time));
			case Unassigned:
				return new Text(String.format("%s was unassigned from this issue %s.", actorName, time));
			case Closed:
				return new Text(String.format("%s closed this issue %s.", actorName, time));
			case Reopened:
				return new Text(String.format("%s reopened this issue %s.", actorName, time));
			case Locked:
				return new Text(String.format("%s locked issue %s.", actorName, time));
			case Unlocked:
				return new Text(String.format("%s unlocked this issue %s.", actorName, time));
			case Referenced:
				return new Text(String.format("%s referenced this issue %s.", actorName, time));
			case Subscribed:
				return new Text(String.format("%s subscribed to receive notifications for this issue %s.", actorName, time));
			case Mentioned:
				return new Text(String.format("%s was mentioned %s.", actorName, time));
			case Merged:
				return new Text(String.format("%s merged this issue %s.", actorName, time));
			case HeadRefDeleted:
				return new Text(String.format("%s deleted the pull request's branch %s.", actorName, time));
			case HeadRefRestored:
				return new Text(String.format("%s restored the pull request's branch %s.", actorName, time));
			default:
				// Not yet implemented, or no events triggered
				return new Text(String.format("%s %s %s.", actorName, getType(), time));
		}
	}

	@Override
	public String toString() {
		String actorName = getActor().getLogin();
		String time = new PrettyTime().format(getDate());

		switch (getType()) {
			case Renamed:
				return String.format("%s renamed this issue %s.", actorName, time);
			case Milestoned:
				return String.format("%s added milestone %s %s.", actorName, getMilestoneTitle(), time);
			case Demilestoned:
				return String.format("%s removed milestone %s %s.", actorName, getMilestoneTitle(), time);
			case Labeled:
				return String.format("%s added label %s %s.", actorName, getLabelName(), time);
			case Unlabeled:
				return String.format("%s removed label %s %s.", actorName, getLabelName(), time);
			case Assigned:
				return String.format("%s was assigned to this issue %s.", actorName, time);
			case Unassigned:
				return String.format("%s was unassigned from this issue %s.", actorName, time);
			case Closed:
				return String.format("%s closed this issue %s.", actorName, time);
			case Reopened:
				return String.format("%s reopened this issue %s.", actorName, time);
			case Locked:
				return String.format("%s locked issue %s.", actorName, time);
			case Unlocked:
				return String.format("%s unlocked this issue %s.", actorName, time);
			case Referenced:
				return String.format("%s referenced this issue %s.", actorName, time);
			case Subscribed:
				return String.format("%s subscribed to receive notifications for this issue %s.", actorName, time);
			case Mentioned:
				return String.format("%s was mentioned %s.", actorName, time);
			case Merged:
				return String.format("%s merged this issue %s.", actorName, time);
			case HeadRefDeleted:
				return String.format("%s deleted the pull request's branch %s.", actorName, time);
			case HeadRefRestored:
				return String.format("%s restored the pull request's branch %s.", actorName, time);
			default:
				// Not yet implemented, or no events triggered
				return String.format("%s %s %s.", actorName, getType(), time);
		}
	}
}
