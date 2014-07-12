package command;

import java.io.IOException;

import service.ServiceManager;
import model.Model;
import model.TurboIssue;
import model.TurboUser;

public class TurboIssueSetAssignee extends TurboIssueCommand{
	private TurboUser newAssignee;
	private TurboUser previousAssignee;
	
	public TurboIssueSetAssignee(Model model, TurboIssue issue, TurboUser user){
		super(model, issue);
		this.newAssignee = user;
	}
	
	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		this.previousAssignee = issue.getAssignee();
		isSuccessful = setIssueAssignee(newAssignee);
		return isSuccessful;
	}
	
	private boolean setIssueAssignee(TurboUser user){
		try {
			ServiceManager.getInstance().addAssigneeToIssue(issue.getId(), user.toGhResource());
			issue.setAssignee(user);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
