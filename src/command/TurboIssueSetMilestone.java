package command;

import java.io.IOException;

import service.ServiceManager;
import model.Model;
import model.TurboIssue;
import model.TurboMilestone;

/**
 * Updates issue milestone on github. 
 * Also sets the milestone of the given TurboIssue object to the given TurboMilestone object
 * */

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
	
	private void logMilestoneChange(TurboMilestone prevMilestone, TurboMilestone newMilestone, boolean logRemarks){
		String changeLog;
		String originalMilestoneTitle = prevMilestone.getTitle();
		String newMilestoneTitle = newMilestone.getTitle();
		if (newMilestoneTitle == null) {
			changeLog = "Milestone removed: [previous: " + originalMilestoneTitle + "]\n";
		} else {
			changeLog = "Milestone changed: [previous: " + originalMilestoneTitle + "] [new: " + newMilestoneTitle + "]\n";
		}
		lastOperationExecuted = changeLog;
		logChangesInGithub(logRemarks, changeLog);
	}

	private boolean setIssueMilestone(TurboMilestone prev, TurboMilestone milestone, boolean logRemarks){
		try {
			ServiceManager.getInstance().setIssueMilestone(issue.getId(), milestone.toGhResource());
			issue.setMilestone(milestone);
			logMilestoneChange(prev, milestone, logRemarks);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean execute() {
		isSuccessful = setIssueMilestone(previousMilestone, newMilestone, true);
		return isSuccessful;
	}

	@Override
	public boolean undo() {
		isUndone = setIssueMilestone(newMilestone, previousMilestone, false);
		return isUndone;
	}
}
