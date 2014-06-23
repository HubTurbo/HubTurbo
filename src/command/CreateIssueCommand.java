package command;

import logic.TurboIssue;

public class CreateIssueCommand implements Command {
	
	private TurboIssue newIssue;
	
	public CreateIssueCommand(TurboIssue issue){
		this.newIssue = issue;
	}

	@Override
	public void execute() {
		
	}

	@Override
	public void undo() {
		// TODO Auto-generated method stub

	}

}
