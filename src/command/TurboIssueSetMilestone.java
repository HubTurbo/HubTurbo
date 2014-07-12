package command;

import java.io.IOException;

import service.ServiceManager;
import model.Model;
import model.TurboIssue;
import model.TurboMilestone;

public class TurboIssueSetMilestone extends TurboIssueCommand{
	private TurboMilestone previousMilestone;
	private TurboMilestone newMilestone = new TurboMilestone();
	
	public TurboIssueSetMilestone(Model model, TurboIssue issue, TurboMilestone milestone){
		super(model, issue);
		this.previousMilestone = issue.getMilestone();
		if(milestone != null){
			this.newMilestone = milestone;
		}
	}
	
	private void logMilestoneChange(TurboMilestone newMilestone, TurboMilestone prevMilestone){
		String changeLog;
		String originalMilestoneTitle = prevMilestone.getTitle();
		String newMilestoneTitle = newMilestone.getTitle();
		if (newMilestoneTitle == null) {
			changeLog = "Milestone removed: [previous: " + originalMilestoneTitle + "]";
		} else {
			changeLog = "Milestone changed: [previous: " + originalMilestoneTitle + "] [new: " + newMilestoneTitle + "]";
		}
		ServiceManager.getInstance().logIssueChanges(issue.getId(), changeLog);
	}

	public boolean setIssueMilestone(TurboMilestone milestone, TurboMilestone prev){
		try {
			ServiceManager.getInstance().setIssueMilestone(issue.getId(), milestone.toGhResource());
			issue.setMilestone(milestone);
			logMilestoneChange(milestone, prev);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean execute() {
		isSuccessful = setIssueMilestone(newMilestone, previousMilestone);
		return isSuccessful;
	}

	@Override
	public boolean undo() {
		isUndone = setIssueMilestone(previousMilestone, newMilestone);
		return isUndone;
	}
}
