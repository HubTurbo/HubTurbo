package command;

import java.lang.ref.WeakReference;

import model.Model;
import model.TurboIssue;

public class TurboIssueAdd extends TurboIssueCommand{

	public TurboIssueAdd(Model model, TurboIssue issue){
		this.issue = issue;
		this.model = new WeakReference<Model>(model);
		this.isUndoableCommand = false;
	}
	
	@Override
	public boolean execute() {
		TurboIssue result = model.get().createIssue(issue);
		if(result != null){
			return true;
		}
		return false;
	}

	@Override
	public boolean undo() {
		return true;
	}

}
