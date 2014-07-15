package command;

import java.io.IOException;

import service.ServiceManager;
import model.Model;
import model.TurboIssue;
import model.TurboUser;

/**
 * Updates issue assignee on github. 
 * Also sets the assignee of the given TurboIssue object to the given TurboUser object
 * */

public class TurboIssueSetAssignee extends TurboIssueCommand{
	private TurboUser newAssignee = new TurboUser();
	private TurboUser previousAssignee;
	
	public TurboIssueSetAssignee(Model model, TurboIssue issue, TurboUser user){
		super(model, issue);
		this.previousAssignee = issue.getAssignee();
		if(user != null){
			this.newAssignee = user;
		}
	}
	
	private void logAssigneeChange(TurboUser assignee){
		String changeLog = "Changed issue assignee to: " + assignee.getGithubName() + "\n";
		ServiceManager.getInstance().logIssueChanges(issue.getId(), changeLog);
		lastOperationExecuted = changeLog;
	}
	
	@Override
	public boolean execute() {
		isSuccessful = setIssueAssignee(newAssignee);
		return isSuccessful;
	}
	
	private boolean setIssueAssignee(TurboUser user){
		try {
			ServiceManager.getInstance().setIssueAssignee(issue.getId(), user.toGhResource());
			issue.setAssignee(user);
			logAssigneeChange(user);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public boolean undo() {
		isUndone = setIssueAssignee(previousAssignee);
		return isUndone;
	}
	
}
