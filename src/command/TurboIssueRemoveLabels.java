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
			e.printStackTrace();
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
			e.printStackTrace();
		}
		
		return isUndone;
	}

}
