package command;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
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
 * Removes list of labels from issue on github
 * Also removes given List of labes from given TurboIssue object
 * */

public class TurboIssueRemoveLabels extends TurboIssueCommand{
	
	private List<TurboLabel> removedLabels;
	
	public TurboIssueRemoveLabels(Model model, TurboIssue issue, List<TurboLabel> labels){
		super(model, issue);
		this.removedLabels = labels;
	}
	
	private void logRemoveOperation(List<TurboLabel> original, List<TurboLabel> edited){
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
		issue.removeLabels(removedLabels);
		try {
			isSuccessful = setLabelsForIssueInGithub();
			if(isSuccessful){
				logRemoveOperation(original, issue.getLabels());
			}
		} catch (IOException e) {
			issue.addLabels(removedLabels);
			isSuccessful = false;
			if(e instanceof SocketTimeoutException | e instanceof UnknownHostException){
				Platform.runLater(()->{
					DialogMessage.showWarningDialog("Internet Connection Timeout", 
							"Timeout removing label(s) from issue in GitHub, please check your internet connection.");
				});
			}else if(e instanceof RequestException){
				Platform.runLater(()->{
					DialogMessage.showWarningDialog("No repository permissions", 
							"Cannot remove label(s) from issue.");
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
		issue.addLabels(removedLabels);
		try {
			boolean result = setLabelsForIssueInGithub();
			if(result){
				logRemoveOperation(original, issue.getLabels());
			}
			isUndone = result;
		} catch (IOException e) {
			issue.removeLabels(removedLabels);
			isUndone = false;
			if(e instanceof SocketTimeoutException){
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
