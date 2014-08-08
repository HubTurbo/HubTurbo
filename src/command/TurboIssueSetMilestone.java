package command;

import java.io.IOException;
import java.net.SocketTimeoutException;

import javafx.application.Platform;

import org.eclipse.egit.github.core.client.RequestException;

import service.ServiceManager;
import util.DialogMessage;
import model.Model;
import model.TurboIssue;
import model.TurboMilestone;

/**
 * Updates issue milestone on github. 
 * Also sets the milestone of the given TurboIssue object to the given TurboMilestone object
 * */

public class TurboIssueSetMilestone extends TurboIssueCommand{
	private TurboMilestone previousMilestone;
	private TurboMilestone newMilestone = new TurboMilestone();
	
	public TurboIssueSetMilestone(Model model, TurboIssue issue, TurboMilestone milestone){
		super(model, issue);
		this.previousMilestone = issue.getMilestone();
		if(milestone != null){
			this.newMilestone = milestone;
		}
	}
	
	private void logMilestoneChange(TurboMilestone prevMilestone, TurboMilestone newMilestone, boolean logRemarks){
		lastOperationExecuted = IssueChangeLogger.logMilestoneChange(issue, prevMilestone, newMilestone);
	}

	private boolean setIssueMilestone(TurboMilestone prev, TurboMilestone milestone, boolean logRemarks){
		try {
			boolean result = ServiceManager.getInstance().setIssueMilestone(issue.getId(), milestone.toGhResource());
			if(result){
				issue.setMilestone(milestone);
				logMilestoneChange(prev, milestone, logRemarks);
			}
			return result;
		} catch (IOException e) {
			if(e instanceof SocketTimeoutException){
				Platform.runLater(()->{
					DialogMessage.showWarningDialog("Internet Connection Timeout", 
							"Timeout modifying milestone for issue in GitHub, please check your internet connection.");
				});
			}else if(e instanceof RequestException){
				Platform.runLater(()->{
					DialogMessage.showWarningDialog("No repository permissions", 
							"Cannot modify issue milestone.");
				});
			}else{
				logger.error(e.getLocalizedMessage(), e);
			}
			return false;
		}
	}

	@Override
	protected boolean performExecuteAction() {
		isSuccessful = setIssueMilestone(previousMilestone, newMilestone, true);
		return isSuccessful;
	}

	@Override
	protected boolean performUndoAction() {
		isUndone = setIssueMilestone(newMilestone, previousMilestone, false);
		return isUndone;
	}
}
