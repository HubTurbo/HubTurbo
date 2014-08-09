package command;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

import javafx.application.Platform;
import model.Model;
import model.TurboIssue;
import model.TurboLabel;

import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.client.RequestException;

import service.ServiceManager;
import util.CollectionUtilities;
import util.DialogMessage;

/**
 * Adds given list of labels to the issue on github. Also adds labels to the given TurboIssue object
 * */

public class TurboIssueAddLabels extends TurboIssueCommand{
	private List<TurboLabel> addedLabels;
	
	public TurboIssueAddLabels(Model model, TurboIssue issue, List<TurboLabel> labels){
		super(model, issue);
		this.addedLabels = labels;
	}
	
	private void logAddOperation(List<TurboLabel> original, List<TurboLabel> edited){
		String changeLog = IssueChangeLogger.logLabelsChange(model.get(), issue, original, edited);
		lastOperationExecuted = changeLog;
	}

	
	private boolean setLabelsForIssueInGithub() throws IOException{
		List<Label> issueLabels = CollectionUtilities.getGithubLabelList(issue.getLabels());
		//Use setLabelsForIssue instead of addLabels to enforce label group exclusivity
		List<Label> resLabels = ServiceManager.getInstance().setLabelsForIssue(issue.getId(), issueLabels);
		boolean result =  resLabels.containsAll(issueLabels);
		if(result){
			updateGithubIssueState();
		}
		return result;
	}
	
	@Override
	protected boolean performExecuteAction() {
		List<TurboLabel> original = issue.getLabels();
		issue.addLabels(addedLabels);
		try {
			isSuccessful = setLabelsForIssueInGithub();
			if(isSuccessful){
				logAddOperation(original, issue.getLabels());
			}
		} catch (IOException e) {
			issue.removeLabels(addedLabels);
			isSuccessful = false;
			if(e instanceof SocketTimeoutException){
				Platform.runLater(()->{
					DialogMessage.showWarningDialog("Internet Connection Timeout", 
							"Timeout adding label(s) to issue in GitHub, please check your internet connection.");
				});
			}else if(e instanceof RequestException){
				Platform.runLater(()->{
					DialogMessage.showWarningDialog("No repository permissions", 
							"Cannot add label(s) to issue.");
				});
			}else{
				logger.error(e.getLocalizedMessage(), e);
			}
		}
		
		return isSuccessful;
	}

	@Override
	protected boolean performUndoAction() {
		List<TurboLabel> original = issue.getLabels();
		issue.removeLabels(addedLabels);
		try {
			boolean result = setLabelsForIssueInGithub();
			if(result){
				logAddOperation(original, issue.getLabels());
			}
			isUndone = result;
		} catch (IOException e) {
			issue.addLabels(addedLabels);
			isUndone = false;
			if(e instanceof SocketTimeoutException | e instanceof UnknownHostException){
				Platform.runLater(()->{
					DialogMessage.showWarningDialog("Internet Connection Timeout", 
							"Timeout modifying label(s) for issue in GitHub, please check your internet connection.");
				});
			}else if(e instanceof RequestException){
				Platform.runLater(()->{
					DialogMessage.showWarningDialog("No repository permissions", 
							"Cannot modify issue labels.");
				});
			}else{
				logger.error(e.getLocalizedMessage(), e);
			}
		}
		return isUndone;
	}

}
