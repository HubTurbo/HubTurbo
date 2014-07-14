package command;

import java.io.IOException;

import org.eclipse.egit.github.core.Issue;

import service.ServiceManager;
import model.Model;
import model.TurboIssue;

/**
 * Creates a new github issue on github and updates model with the created issue data
 * */

public class TurboIssueAdd extends TurboIssueCommand{

	public TurboIssueAdd(Model model, TurboIssue issue){
		super(model, issue);
		this.isUndoableCommand = false;
	}
	
	private TurboIssue createIssueInGithub(TurboIssue newIssue) {
		Issue ghIssue = newIssue.toGhResource();
		Issue createdIssue = null;
		try {
			createdIssue = ServiceManager.getInstance().createIssue(ghIssue);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		TurboIssue returnedIssue = new TurboIssue(createdIssue, model.get());
		return returnedIssue;
	}
	
	private void addIssueToLocalCache(TurboIssue issue){
		model.get().appendToCachedIssues(issue);
	}
	
	@Override
	public boolean execute() {
		TurboIssue result = createIssueInGithub(issue);
		if(result != null){
			addIssueToLocalCache(result);
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
