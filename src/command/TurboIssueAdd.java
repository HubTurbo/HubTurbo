package command;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javafx.application.Platform;
import model.Model;
import model.TurboIssue;

import org.eclipse.egit.github.core.Issue;

import service.ServiceManager;
import util.DialogMessage;

/**
 * Creates a new github issue on github and updates model with the created issue data
 * */

public class TurboIssueAdd extends TurboIssueCommand{
	private static String ADD_ISSUE_LOG = "Added issue: #%1d %2s \n";
	TurboIssue addResult;
	
	public TurboIssueAdd(Model model, TurboIssue issue){
		super(model, issue);
		this.isUndoableCommand = false;
	}
	
	private TurboIssue createIssueInGithub(TurboIssue newIssue) {
		Issue ghIssue = newIssue.toGhResource();
		Issue createdIssue = null;
		try {
			createdIssue = ServiceManager.getInstance().createIssue(ghIssue);
		} catch (SocketTimeoutException | UnknownHostException e){
			Platform.runLater(()->{
				DialogMessage.showWarningDialog("Internet Connection Timeout", 
						"Timeout adding issue in GitHub, please check your internet connection.");
			});
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
		} 
		TurboIssue returnedIssue = new TurboIssue(createdIssue, model.get());
		return returnedIssue;
	}
	
	private void logIssueAdd(){
		lastOperationExecuted = String.format(ADD_ISSUE_LOG, issue.getId(), issue.getTitle());
	}
	
	private void addIssueToLocalCache(TurboIssue issue){
		Platform.runLater(() -> {
			model.get().appendToCachedIssues(issue);
		});
	}
	
	public TurboIssue getAddedIssue(){
		return addResult;
	}
	
	@Override
	protected boolean performExecuteAction() {
		addResult = createIssueInGithub(issue);
		if(addResult != null){
			addIssueToLocalCache(addResult);
			logIssueAdd();
			isSuccessful = true;
		}else{
			isSuccessful = false;
		}
		return isSuccessful;
	}

	@Override
	protected boolean performUndoAction() {
		return true;
	}

}
