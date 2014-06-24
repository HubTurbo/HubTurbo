package command;

import model.TurboIssue;

import org.eclipse.egit.github.core.Issue;

public class CreateIssueCommand implements UndoableCommand {
	
	private TurboIssue newIssue;
	
	public CreateIssueCommand(TurboIssue issue){
		this.newIssue = issue;
		
	}

	@Override
	public void execute() {
		Issue ghIssue = newIssue.toGhIssue();
		
	}

	@Override
	public void undo() {
		// TODO Auto-generated method stub
		
	}


}
