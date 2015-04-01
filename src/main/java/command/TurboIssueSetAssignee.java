package command;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javafx.application.Platform;

import org.eclipse.egit.github.core.client.RequestException;

import service.ServiceManager;
import util.DialogMessage;
import model.Model;
import model.TurboIssue;
import model.TurboUser;

/**
 * Updates issue assignee on github. 
 * Also sets the assignee of the given TurboIssue object to the given TurboUser object
 * */

public class TurboIssueSetAssignee extends TurboIssueCommand{
	private TurboUser newAssignee = new TurboUser();
	private TurboUser previousAssignee;
	
	public TurboIssueSetAssignee(Model model, TurboIssue issue, TurboUser user){
		super(model, issue);
		this.previousAssignee = issue.getAssignee();
		if(user != null){
			this.newAssignee = user;
		}
	}
	
	private void logAssigneeChange(TurboUser original, TurboUser edited){
		String changeLog = IssueChangeLogger.logAssigneeChange(issue, original, edited);
		lastOperationExecuted = changeLog;
	}
	
	@Override
	protected boolean performExecuteAction() {
		isSuccessful = setIssueAssignee(newAssignee, true);
		return isSuccessful;
	}
	
	private boolean setIssueAssignee(TurboUser user, boolean logRemarks){
		try {
			TurboUser original = issue.getAssignee();
			boolean result = ServiceManager.getInstance().setIssueAssignee(issue.getId(), user.toGhResource());
			if(result){
				issue.setAssignee(user);
				logAssigneeChange(original, user);
			}
			return result;
		} catch (IOException e) {
			if(e instanceof SocketTimeoutException | e instanceof UnknownHostException){
				Platform.runLater(()->{
					DialogMessage.showWarningDialog("Internet Connection Timeout", 
							"Timeout modifying assignee for issue in GitHub, please check your internet connection.");
				});
				logger.info("Could not change issue assignee: " + e.getLocalizedMessage());
			}else if(e instanceof RequestException){
				Platform.runLater(()->{
					DialogMessage.showWarningDialog("No repository permissions", 
							"Cannot modify issue assignee.");
				});
				logger.info("Could not change issue assignee: " + e.getLocalizedMessage());
			}else{
				logger.error(e.getLocalizedMessage(), e);
			}
			return false;
		}
	}
	
	@Override
	protected boolean performUndoAction() {
		isUndone = setIssueAssignee(previousAssignee, false);
		return isUndone;
	}
	
}
