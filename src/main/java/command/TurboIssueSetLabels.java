package command;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;

import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.client.RequestException;

import service.ServiceManager;
import util.CollectionUtilities;
import util.DialogMessage;
import model.Model;
import model.TurboIssue;
import model.TurboLabel;

/**
 * Sets labels of issue on github
 * Also sets labels of given TurboIssue object to given list of TurboLabels
 * */

public class TurboIssueSetLabels extends TurboIssueCommand{
	private List<TurboLabel> previousLabels;
	private List<TurboLabel> newLabels;
	
	public TurboIssueSetLabels(Model model, TurboIssue issue, List<TurboLabel> labels){
		super(model, issue);
		this.newLabels = labels;
		this.previousLabels = issue.getLabels(); //Is a copy of original list of labels
	}
	
	@Override
	protected boolean performExecuteAction() {
		isSuccessful = setLabelsForIssue(previousLabels, newLabels, true);
		return isSuccessful;
	}
	
	private boolean setGithubLabelsForIssue(List<Label> ghLabels) throws IOException{
		List<Label> res = ServiceManager.getInstance().setLabelsForIssue(issue.getId(), ghLabels);
		boolean result = res.containsAll(ghLabels);
		if(result){
			updateGithubIssueState();
		}
		return result;
	}
	
	private boolean setLabelsForIssue(List<TurboLabel> oldLabels, List<TurboLabel>updatedLabels, boolean logRemarks){
		issue.setLabels(updatedLabels);
		ArrayList<Label> ghLabels = CollectionUtilities.getGithubLabelList(updatedLabels);
		try {
			boolean result = setGithubLabelsForIssue(ghLabels);
			if(result){
				logLabelsChange(oldLabels, updatedLabels, logRemarks);
			}
			return result;
		} catch (IOException e) {
			issue.setLabels(oldLabels);
			if(e instanceof SocketTimeoutException | e instanceof UnknownHostException){
				Platform.runLater(()->{
					DialogMessage.showWarningDialog("Internet Connection Timeout", 
							"Timeout modifying labels for issue in GitHub, please check your internet connection.");
				});
				logger.info("Could not set issue labels: " + e.getLocalizedMessage());
			}else if(e instanceof RequestException){
				Platform.runLater(()->{
					DialogMessage.showWarningDialog("No repository permissions", 
							"Cannot modify issue labels.");
				});
				logger.info("Could not set issue labels: " + e.getLocalizedMessage());
			}else{
				logger.error(e.getLocalizedMessage(), e);
			}
			return false;
		}
	}
	
	private void logLabelsChange(List<TurboLabel> oldLabels, List<TurboLabel> labels, boolean logRemarks){		
		lastOperationExecuted = IssueChangeLogger.logLabelsChange(model.get(), issue, oldLabels, labels);
	}
	
	@Override
	protected boolean performUndoAction() {
		if(isSuccessful){
			isUndone = setLabelsForIssue(newLabels, previousLabels, false);
		}
		return isUndone;
	}

}
