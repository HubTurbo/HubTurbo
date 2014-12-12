package ui.issuepanel.expanded.comments;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import service.TurboIssueEvent;

public class IssueEventsCard extends VBox{
	
	private TurboIssueEvent event;
	
	public IssueEventsCard(TurboIssueEvent item) {
		this.event = item;

		initialiseUIComponents();
	}
	
	private void initialiseUIComponents() {
		getChildren().add(new Label(event.getDate() + ": " + getMessage()));
	}

	private String getMessage() {
		String message = "";
		String actorName = event.getActor().getLogin();
		switch (event.getType()) {
		case Renamed:
			message = String.format("%s renamed the issue from '%s' to '%s'.",
					actorName,
					event.getRenamedFrom(),
					event.getRenamedTo());
		    break;
		case Milestoned:
			message = String.format("%s added the milestone '%s'.",
					actorName,
					event.getMilestoneTitle());
		case Demilestoned:
			message = String.format("%s removed the milestone '%s'.",
					actorName,
					event.getMilestoneTitle());
		    break;
		case Labeled:
			message = String.format("%s added the label '%s'.",
					actorName,
					event.getLabelName());
		case Unlabeled:
			message = String.format("%s removed the label '%s'.",
					actorName,
					event.getLabelName());
		    break;
		case Assigned:
			message = String.format("%s assigned %s to the issue.",
					actorName,
					event.getAssignedUser().getLogin());
		case Unassigned:
			message = String.format("%s unassigned %s from the issue.",
					actorName,
					event.getAssignedUser().getLogin());
			break;
		case Closed:
			message = String.format("%s closed the issue.",
					actorName);
		case Reopened:
			message = String.format("%s reopened the issue.",
					actorName);
		case Locked:
			message = String.format("%s locked the issue.",
					actorName);
		case Unlocked:
			message = String.format("%s unlocked the issue.",
					actorName);
			break;
		case Subscribed:
		case Merged:
		case HeadRefDeleted:
		case HeadRefRestored:
		case Referenced:
		case Mentioned:
		default:
			// Not yet implemented, or no events triggered
		}
		return message;
	}
}
