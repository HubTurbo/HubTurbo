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
	
	private void logAddOperation(boolean added){
		String changeLog;
		if(added){
			changeLog = LABELS_ADD_LOG_PREFIX + addedLabels.toString() + "\n";
		}else{
			changeLog = LABELS_REMOVE_LOG_PREFIX + addedLabels.toString() + "\n";
		}
		lastOperationExecuted = changeLog;
		logChangesInGithub(added, changeLog);
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
		issue.addLabels(addedLabels);
		try {
			addLabelsToIssueInGithub();
			logAddOperation(true);
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
		issue.removeLabels(addedLabels);
		try {
			removeLabelsFromIssueInGithub(ghLabels);
			logAddOperation(false);
			isUndone = true;
		} catch (IOException e) {
			issue.addLabels(addedLabels);
			e.printStackTrace();
			isUndone = false;
		}
		return isUndone;
	}

}
