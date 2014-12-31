package model;

import java.util.Date;
import java.util.function.Predicate;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

import org.eclipse.egit.github.core.IssueEvent;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.User;

import service.IssueEventType;
import service.TurboIssueEvent;
import ui.FilterTextField;
import util.Utility;
import filter.expression.FilterExpression;
import filter.expression.Qualifier;
import filter.Parser;

import org.ocpsoft.prettytime.PrettyTime;

public class TurboFeed implements Listable {
	
	public static final FilterExpression EMPTY = Qualifier.EMPTY;
	
	private Predicate<TurboIssue> predicate = p -> true;
	private FilterExpression currentFilterExpression = EMPTY;
	private FilterTextField filterTextField;
	
	private IssueEvent issueEvent;
	
	public TurboFeed() {
		super();
//		getChildren().add(createFilterBox());
	}
	
	public TurboFeed(IssueEvent issueEvent) {
		assert issueEvent != null;

		this.issueEvent = issueEvent;
	}
	
	public IssueEvent getIssueEvent(){
		return issueEvent;
	}

	public void setIssueEvent(IssueEvent ie){
		this.issueEvent = ie;
	}
	
	@Override
	public String getListName() {
    	PrettyTime pt = new PrettyTime();
        TurboIssueEvent event = new TurboIssueEvent(this.issueEvent.getActor(), 
        		IssueEventType.fromString(this.issueEvent.getEvent()), 
        		this.issueEvent.getCreatedAt());
        /*
        event.setAssignedUser(assignedUser);
        event.setLabelColour(labelColour);
        event.setLabelName(labelName);
        event.setMilestoneTitle(milestoneTitle);
        event.setRenamedFrom(renamedFrom);
        event.setRenamedTo(renamedTo);
        */

        String message = "";
        String actorName = event.getActor().getLogin();
        switch (event.getType()) {
        case Renamed:
            message = String.format("%s renamed the issue.",
                    actorName);
            break;
        case Milestoned:
            message = String.format("%s added the milestone.",
                    actorName);
            break;
        case Demilestoned:
            message = String.format("%s removed the milestone.",
                    actorName);
            break;
        case Labeled:
            message = String.format("%s added the label.",
                    actorName);
            break;
        case Unlabeled:
            message = String.format("%s removed the label.",
                    actorName);
            break;
        case Assigned:
            message = String.format("%s assigned to the issue.",
                    actorName);
            break;
        case Unassigned:
            message = String.format("%s unassigned from the issue.",
                    actorName);
            break;
        case Closed:
            message = String.format("%s closed the issue.",
                    actorName);
            break;
        case Reopened:
            message = String.format("%s reopened the issue.",
                    actorName);
            break;
        case Locked:
            message = String.format("%s locked the issue.",
                    actorName);
            break;
        case Unlocked:
            message = String.format("%s unlocked the issue.",
                    actorName);
            break;
        case Subscribed:
        case Merged:
        case HeadRefDeleted:
        case HeadRefRestored:
        case Referenced:
            message = String.format("%s referenced the issue.",
                    actorName);
            break;
        case Mentioned:
        default:
            // Not yet implemented, or no events triggered
        }
        if (message.length() == 0) {
        	return "";
        } else {
    		return pt.format(this.issueEvent.getCreatedAt()) + "\n" + message;
        }
    }

	@Override
	public void copyValues(Object other) {
		// TODO Auto-generated method stub
	}
	
	private Node createFilterBox() {
//		String initialText = isSearchPanel ? "title()" : "";
//		int initialPosition = isSearchPanel ? 6 : 0;
		
		filterTextField = new FilterTextField("", 0)
			.setOnConfirm((text) -> {
//				if (Parser.isListOfSymbols(text)) {
//					text = "title(" + text + ")";
//				}
//				applyStringFilter(text);
				return text;
			})
			.setOnCancel(() -> {
//				parentColumnControl.closeColumn(columnIndex);
			});

//		setupIssueDragEvents(filterTextField);
		setupIssueFocusEvents(filterTextField);
	
		HBox buttonsBox = new HBox();
		buttonsBox.setSpacing(5);
		buttonsBox.setAlignment(Pos.TOP_RIGHT);
		buttonsBox.setMinWidth(50);
//		buttonsBox.getChildren().addAll(createButtons());
		
		HBox layout = new HBox();
		layout.getChildren().addAll(filterTextField, buttonsBox);
		layout.setPadding(new Insets(0,0,3,0));		
		
//		setupColumnDragEvents(layout);
		return layout;
	}
	
	private void setupIssueFocusEvents(FilterTextField field) {
		field.focusedProperty().addListener((obs, old, newValue) -> {
			if (newValue) {
				// Gained focus
//				parentColumnControl.setCurrentlyFocusedColumnIndex(columnIndex);
			} else {
				// Lost focus
				// Do nothing
			}
		});
	}
}
