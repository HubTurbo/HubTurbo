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
 * Updates issue description on github. 
 * Also sets the description of the given TurboIssue object to the given description String.
 * */

public class TurboIssueEditDescription extends TurboIssueCommand{
	String newDescription;

	public TurboIssueEditDescription(Model model, TurboIssue issue, String description){
		super(model, issue);
		newDescription = description;
	}
	
	@Override
	protected boolean performExecuteAction() {
		String oldDescription = issue.getDescription();
		isSuccessful = editIssueDescription(oldDescription, newDescription);
		return isSuccessful;
	}
	
	private void logDescriptionChange(String original, String edited){
		String changeLog = IssueChangeLogger.logDescriptionChange(issue, original, edited);
		lastOperationExecuted = changeLog;
	}
	
	private boolean editIssueDescription(String oldDesc, String newDesc){
		issue.setDescription(newDesc);
		try {
			ServiceManager.getInstance().editIssueBody(issue.getId(), issue.buildGithubBody());
			logDescriptionChange(oldDesc, newDesc);
			
			return true;
		} catch (IOException e) {
			issue.setDescription(oldDesc);
			if(e instanceof SocketTimeoutException){
				Platform.runLater(()->{
					DialogMessage.showWarningDialog("Internet Connection Timeout", 
							"Timeout modifying description for issue in GitHub, please check your internet connection.");
				});
			}else if(e instanceof RequestException){
				Platform.runLater(()->{
					DialogMessage.showWarningDialog("No repository permissions", 
							"Cannot modify issue description.");
				});
			}else{
				logger.error(e.getLocalizedMessage(), e);
			}
			return false;
		}
	}

	@Override
	protected boolean performUndoAction() {
		return true;
	}

}
