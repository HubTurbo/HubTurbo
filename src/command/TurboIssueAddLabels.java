package command;

import java.io.IOException;
import java.util.List;

import org.eclipse.egit.github.core.Label;

import service.ServiceManager;
import util.CollectionUtilities;
import model.Model;
import model.TurboIssue;
import model.TurboLabel;

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
			e.printStackTrace();
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
			e.printStackTrace();
			isUndone = false;
		}
		return isUndone;
	}

}
