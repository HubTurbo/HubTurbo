package command;

import java.io.IOException;
import java.net.SocketTimeoutException;

import javafx.application.Platform;

import org.eclipse.egit.github.core.client.RequestException;

import service.ServiceManager;
import util.DialogMessage;
import model.Model;
import model.TurboIssue;

/**
 * Updates issue title on github. 
 * Also sets the title of the given TurboIssue object to the given description String.
 * */

public class TurboIssueEditTitle extends TurboIssueCommand{
	
	private String newTitle;
	
	public TurboIssueEditTitle(Model model, TurboIssue issue, String title){
		super(model, issue);
		this.newTitle = title;
	}
	
	private void logTitleChange(String prevTitle, String newTitle){
		String changeLog = IssueChangeLogger.logTitleChange(issue, prevTitle, newTitle);
		lastOperationExecuted = changeLog;
	}

	@Override
	protected boolean performExecuteAction() {
		String previousTitle = issue.getTitle();
		try {
			ServiceManager.getInstance().editIssueTitle(issue.getId(), newTitle);
			issue.setTitle(newTitle);
			logTitleChange(previousTitle, newTitle);
			isSuccessful = true;
		} catch (IOException e) {
			isSuccessful = false;
			if(e instanceof SocketTimeoutException){
				Platform.runLater(()->{
					DialogMessage.showWarningDialog("Internet Connection Timeout", 
							"Timeout modifying title for issue in GitHub, please check your internet connection.");
				});
			}else if(e instanceof RequestException){
				Platform.runLater(()->{
					DialogMessage.showWarningDialog("No repository permissions", 
							"Cannot modify issue title.");
				});
			}else{
				logger.error(e.getLocalizedMessage(), e);
			}
		}
		return isSuccessful;
	}

	@Override
	protected boolean performUndoAction() {
		return true;
	}
}
