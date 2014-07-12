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

	public boolean setIssueMilestone(TurboMilestone milestone){
		try {
			ServiceManager.getInstance().addMilestoneToIssue(issue.getId(), milestone.toGhResource());
			issue.setMilestone(milestone);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean execute() {
		isSuccessful = setIssueMilestone(newMilestone);
		return isSuccessful;
	}

	@Override
	public boolean undo() {
		isUndone = setIssueMilestone(previousMilestone);
		return isUndone;
	}
}
