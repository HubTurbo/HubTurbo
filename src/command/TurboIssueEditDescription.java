package command;

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
		issue.setDescription(newDescription);
		return false;
	}

	@Override
	public boolean undo() {
		return true;
	}

}
