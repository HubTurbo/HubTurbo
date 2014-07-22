package command;

import java.io.IOException;
import java.util.ArrayList;
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

	
	private void addLabelsToIssueInGithub() throws IOException{
		List<Label> issueLabels = CollectionUtilities.getGithubLabelList(issue.getLabels());
		//Use setLabelsForIssue instead of addLabels to enforce label group exclusivity
		ServiceManager.getInstance().setLabelsForIssue(issue.getId(), issueLabels);
		updateGithubIssueState();
	}
	
	private void removeLabelsFromIssueInGithub(List<Label> ghLabels) throws IOException{
		ServiceManager.getInstance().deleteLabelsFromIssue(issue.getId(), ghLabels);
		updateGithubIssueState();
	}
	
	@Override
	public boolean execute() {
		List<TurboLabel> original = issue.getLabels();
		issue.addLabels(addedLabels);
		try {
			addLabelsToIssueInGithub();
			logAddOperation(original, issue.getLabels());
			isSuccessful = true;
		} catch (IOException e) {
			issue.removeLabels(addedLabels);
			isSuccessful = false;
			e.printStackTrace();
		}
		
		return isSuccessful;
	}

	@Override
	public boolean undo() {
		ArrayList<Label> ghLabels = CollectionUtilities.getGithubLabelList(addedLabels);
		List<TurboLabel> original = issue.getLabels();
		issue.removeLabels(addedLabels);
		try {
			removeLabelsFromIssueInGithub(ghLabels);
			logAddOperation(original, issue.getLabels());
			isUndone = true;
		} catch (IOException e) {
			issue.addLabels(addedLabels);
			e.printStackTrace();
			isUndone = false;
		}
		return isUndone;
	}

}
