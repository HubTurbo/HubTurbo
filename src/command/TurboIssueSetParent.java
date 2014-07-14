package command;

import model.Model;
import model.TurboIssue;

public class TurboIssueSetParent extends TurboIssueCommand{
	private Integer newParent;
	private Integer previousParent;
	
	public TurboIssueSetParent(Model model, TurboIssue issue, Integer parent){
		super(model, issue);
		newParent = parent;
		previousParent = issue.getParentIssue();
	}
	
//	private boolean setIssueParent(Integer parent){
//		
//	}
	
	@Override
	public boolean execute() { 
		return false;
	}

	@Override
	public boolean undo() {
		// TODO Auto-generated method stub
		return false;
	}

}
