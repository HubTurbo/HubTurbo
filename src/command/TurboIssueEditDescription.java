package command;

import model.Model;
import model.TurboIssue;

public class TurboIssueEditDescription extends TurboIssueCommand{
	
	public TurboIssueEditDescription(Model model, TurboIssue issue, String description){
		super(model, issue);
	}
	
	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean undo() {
		// TODO Auto-generated method stub
		return false;
	}

}
