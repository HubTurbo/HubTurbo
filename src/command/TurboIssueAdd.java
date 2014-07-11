package command;

import java.io.IOException;
import java.lang.ref.WeakReference;

import org.eclipse.egit.github.core.Issue;

import service.ServiceManager;
import model.Model;
import model.TurboIssue;

public class TurboIssueAdd extends TurboIssueCommand{

	public TurboIssueAdd(Model model, TurboIssue issue){
		this.issue = issue;
		this.model = new WeakReference<Model>(model);
		this.isUndoableCommand = false;
	}
	
	private TurboIssue createIssueInGithub(TurboIssue newIssue) {
		Model mod = model.get();
		Issue ghIssue = newIssue.toGhResource();
		Issue createdIssue = null;
		try {
			createdIssue = ServiceManager.getInstance().createIssue(ghIssue);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		TurboIssue returnedIssue = new TurboIssue(createdIssue, mod);
		mod.appendToCachedIssues(returnedIssue);
		return returnedIssue;
	}
	
	@Override
	public boolean execute() {
		TurboIssue result = createIssueInGithub(issue);
		if(result != null){
			isSuccessful = true;
		}else{
			isSuccessful = false;
		}
		return isSuccessful;
	}

	@Override
	public boolean undo() {
		return true;
	}

}
