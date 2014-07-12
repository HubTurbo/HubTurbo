package command;

import java.io.IOException;

import service.ServiceManager;
import model.Model;
import model.TurboIssue;

public class TurboIssueEditDescription extends TurboIssueCommand{
	String newDescription;

	public TurboIssueEditDescription(Model model, TurboIssue issue, String description){
		super(model, issue);
		newDescription = description;
	}
	
	@Override
	public boolean execute() {
		String oldDescription = issue.getDescription();
		isSuccessful = setIssueDescription(newDescription, oldDescription);
		return isSuccessful;
	}
	
	private boolean setIssueDescription(String newDesc, String oldDesc){
		issue.setDescription(newDesc);
		try {
			ServiceManager.getInstance().editIssueBody(issue.getId(), issue.buildGithubBody());
			return true;
		} catch (IOException e) {
			issue.setDescription(oldDesc);
			return false;
		}
	}

	@Override
	public boolean undo() {
		return true;
	}

}
