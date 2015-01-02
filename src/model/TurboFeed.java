package model;

import java.util.function.Predicate;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.IssueEvent;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.User;

import service.IssueEventType;
import service.TurboIssueEvent;
import ui.FilterTextField;
import filter.expression.FilterExpression;
import filter.expression.Qualifier;

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
        String milestoneTitle = "";

        Issue currIssue = this.issueEvent.getIssue();
        int issueNum = currIssue.getNumber();
        
        TurboIssueEvent event = new TurboIssueEvent(this.issueEvent.getActor(), 
        		IssueEventType.fromString(this.issueEvent.getEvent()), 
        		this.issueEvent.getCreatedAt());

        String message = "";
        String actorName = event.getActor().getLogin();
        switch (event.getType()) {
        case Renamed:
            message = String.format("%s renamed issue #%d.",
                    actorName, issueNum);
            break;
        case Milestoned:
            if (currIssue.getMilestone() != null) {
            	milestoneTitle = currIssue.getMilestone().getTitle();
            }
            message = String.format("%s added issue #%d to %s milestone.",
                    actorName, issueNum, milestoneTitle);
            break;
        case Demilestoned:
            if (currIssue.getMilestone() != null) {
            	milestoneTitle = currIssue.getMilestone().getTitle();
            }
            message = String.format("%s removed issue #%d from %s milestone.",
                    actorName, issueNum, milestoneTitle);
            break;
        case Labeled:
            message = String.format("%s added label to issue #%d.",
                    actorName, issueNum);
            break;
        case Unlabeled:
            message = String.format("%s removed label from issue #%d.",
                    actorName, issueNum);
            break;
        case Assigned:
        	String assignee = "";
        	if (currIssue.getAssignee() != null) {
        		assignee = currIssue.getAssignee().getLogin();
        	}
            message = String.format("%s was assigned to issue #%d.",
            		assignee, issueNum);
            break;
        case Unassigned:
            message = String.format("%s was unassigned from issue #%d.",
                    actorName, issueNum);
            break;
        case Closed:
            message = String.format("%s closed issue #%d.",
                    actorName, issueNum);
            break;
        case Reopened:
            message = String.format("%s reopened issue #%d.",
                    actorName, issueNum);
            break;
        case Locked:
            message = String.format("%s locked issue #%d.",
                    actorName, issueNum);
            break;
        case Unlocked:
            message = String.format("%s unlocked issue #%d.",
                    actorName, issueNum);
            break;
        case Subscribed:
        case Merged:
        case HeadRefDeleted:
        case HeadRefRestored:
        case Referenced:
            message = String.format("%s referenced issue #%d.",
                    actorName, issueNum);
            break;
        case Mentioned:
        default:
            // Not yet implemented, or no events triggered
        }
        if (message.length() == 0) {
        	return "";
        } else {
    		return pt.format(this.issueEvent.getCreatedAt()) + "\n" + message
    				+ "\n" + this.issueEvent.getEvent();
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
