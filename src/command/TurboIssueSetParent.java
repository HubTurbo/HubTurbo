package command;

import model.Model;
import model.TurboIssue;

public class TurboIssueSetParent extends TurboIssueCommand{
	
	public TurboIssueSetParent(Model model, TurboIssue issue, Integer parent){
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
