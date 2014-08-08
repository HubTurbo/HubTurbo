package command;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import javafx.application.Platform;

import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.client.RequestException;

import service.ServiceManager;
import util.CollectionUtilities;
import util.DialogMessage;
import model.Model;
import model.TurboIssue;

/**
 * Updates issue parent on github. 
 * Also sets the parent of the given TurboIssue object to the given parent issue number
 * */

public class TurboIssueSetParent extends TurboIssueCommand{
	private Integer newParent;
	private Integer previousParent;
	
	public TurboIssueSetParent(Model model, TurboIssue issue, Integer parent){
		super(model, issue);
		newParent = parent;
		previousParent = issue.getParentIssue();
	}
	
	private void logParentChange(Integer oldParent, Integer parent, boolean logRemarks){
		lastOperationExecuted = IssueChangeLogger.logParentChange(issue, oldParent, parent);
	}
	
	private void setLocalIssueParent(Integer oldParent, Integer parent){
		issue.setParentIssue(parent);
		processInheritedLabels(oldParent, parent, issue);
	}
	
	private void updateGithubIssueParent(Integer oldParent, Integer parent) throws IOException{
		ServiceManager service = ServiceManager.getInstance();
		service.editIssueBody(issue.getId(), issue.buildGithubBody());
		ArrayList<Label> ghLabels = CollectionUtilities.getGithubLabelList(issue.getLabels());
		service.setLabelsForIssue(issue.getId(), ghLabels);
	}
	
	private boolean setIssueParent(Integer oldParent, Integer parent, boolean logRemarks){
		setLocalIssueParent(oldParent, parent);
		try {
			updateGithubIssueParent(oldParent, parent);
			logParentChange(oldParent, parent, logRemarks);
			return true;
		} catch (IOException e) {
			setLocalIssueParent(parent, oldParent);
			if(e instanceof SocketTimeoutException){
				Platform.runLater(()->{
					DialogMessage.showWarningDialog("Internet Connection Timeout", 
							"Timeout modifying parent for issue in GitHub, please check your internet connection.");
				});
			}else if(e instanceof RequestException){
				Platform.runLater(()->{
					DialogMessage.showWarningDialog("No repository permissions", 
							"Cannot modify issue parent.");
				});
			}else{
				logger.error(e.getLocalizedMessage(), e);
			}
			return false;
		}
	}
	
	@Override
	protected boolean performExecuteAction() {
		isSuccessful = setIssueParent(previousParent, newParent, true);
		return isSuccessful;
	}

	@Override
	protected boolean performUndoAction() {
		isUndone = setIssueParent(newParent, previousParent, false);
		return isUndone;
	}

}
