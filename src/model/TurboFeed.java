package model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.IssueEvent;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.User;

import service.IssueEventType;
import filter.expression.FilterExpression;
import filter.expression.Qualifier;

import org.ocpsoft.prettytime.PrettyTime;

public class TurboFeed implements Listable {
	
	public static final FilterExpression EMPTY = Qualifier.EMPTY;
	private ArrayList<IssueEvent> eventList = new ArrayList<>();
	private Issue currIssue;
	private int issueNum;
	
	/*
	public TurboFeed() {
		super();
	}
	*/
	
	public TurboFeed(IssueEvent event) {
		assert event != null;
		eventList.add(event);
		this.currIssue = event.getIssue();
		this.issueNum = currIssue.getNumber();
	}
	
	public int getIssueNum(){
        return issueNum;
	}

	public IssueEvent getIssueEvent(int index){
		return eventList.get(index);
	}

	public void addIssueEvent(IssueEvent event){
		if (issueNum == event.getIssue().getNumber()) {
			eventList.add(event);
		} else {
			System.out.println("Unable to add TurboFeed: Issue Number Mismatched");
		}
	}
	
	public List<Label> getLabels(){
		return currIssue.getLabels();
	}

	@Override
	public String getListName() {
    	return getFeed();
    }

	@Override
	public void copyValues(Object other) {
		// TODO Auto-generated method stub
		
	}

	public String getFeed() {
        String message = "";
        /*
        if (this.issueEvent.getEvent().equalsIgnoreCase("pullrequest")) {
            message = String.format("%s made a pull request.",
                    actorName);
        } else {
        	message = formatMessage();
        }
        */
    	message = "#" + issueNum;
		for(IssueEvent event: eventList){
	    	message = message + "\n" + formatMessage(event);
		}
    	return message;
    }

	private String formatMessage(IssueEvent currEvent) {
    	PrettyTime pt = new PrettyTime();
		String timeString = pt.format(currEvent.getCreatedAt());
        String actorName = currEvent.getActor().getLogin();
		Issue currIssue = currEvent.getIssue();
        String milestoneTitle = "";
        String message = "";
        
        switch (IssueEventType.fromString(currEvent.getEvent())) {
        case Renamed:
            message = String.format("%s renamed issue %s.",
                    actorName, timeString);
            break;
        case Milestoned:
            if (currIssue.getMilestone() != null) {
            	milestoneTitle = currIssue.getMilestone().getTitle();
            }
            message = String.format("%s added to %s milestone %s.",
                    actorName, milestoneTitle, timeString);
            break;
        case Demilestoned:
            if (currIssue.getMilestone() != null) {
            	milestoneTitle = currIssue.getMilestone().getTitle();
            }
            message = String.format("%s removed from %s milestone %s.",
                    actorName, milestoneTitle, timeString);
            break;
        case Labeled:
            message = String.format("%s added label %s.",
                    actorName, timeString);
            break;
        case Unlabeled:
            message = String.format("%s removed label %s.",
                    actorName, timeString);
            break;
        case Assigned:
        	String assignee = "";
        	if (currIssue.getAssignee() != null) {
        		assignee = currIssue.getAssignee().getLogin();
        	}
            message = String.format("%s was assigned %s.",
            		assignee, timeString);
            break;
        case Unassigned:
            message = String.format("%s was unassigned %s.",
                    actorName, timeString);
            break;
        case Closed:
            message = String.format("%s closed issue %s.",
                    actorName, timeString);
            break;
        case Reopened:
            message = String.format("%s reopened issue %s.",
                    actorName, timeString);
            break;
        case Locked:
            message = String.format("%s locked issue %s.",
                    actorName, timeString);
            break;
        case Unlocked:
            message = String.format("%s unlocked issue %s.",
                    actorName, timeString);
            break;
        case Referenced:
            message = String.format("%s referenced issue %s.",
                    actorName, timeString);
            break;
        case Subscribed:
        case Mentioned:
            message = String.format("%s commented %s.",
                    actorName, timeString);
        	break;
        case Merged:
        case HeadRefDeleted:
        case HeadRefRestored:
        default:
            // Not yet implemented, or no events triggered
        }
    	return message;
    }
}