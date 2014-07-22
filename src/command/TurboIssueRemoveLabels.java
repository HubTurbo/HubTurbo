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
 * Removes list of labels from issue on github
 * Also removes given List of labes from given TurboIssue object
 * */

public class TurboIssueRemoveLabels extends TurboIssueCommand{
	
	private List<TurboLabel> removedLabels;
	
	public TurboIssueRemoveLabels(Model model, TurboIssue issue, List<TurboLabel> labels){
		super(model, issue);
		this.removedLabels = labels;
	}
	
	private void logRemoveOperation(boolean remove){
		String changeLog;
		if(remove){
			changeLog = LABELS_REMOVE_LOG_PREFIX + removedLabels.toString() + "\n";
		}else{
			changeLog = LABELS_ADD_LOG_PREFIX + removedLabels.toString() + "\n";
		}
		lastOperationExecuted = changeLog;
		logChangesInGithub(remove, changeLog);
	}
	

	@Override
	public boolean execute() {
		ServiceManager service = ServiceManager.getInstance();
		ArrayList<Label> ghLabels = CollectionUtilities.getGithubLabelList(removedLabels);
		issue.removeLabels(removedLabels);
		try {
			service.deleteLabelsFromIssue(issue.getId(), ghLabels);
			updateGithubIssueState();
			logRemoveOperation(true);
			isSuccessful = true;
		} catch (IOException e) {
			issue.addLabels(removedLabels);
			isSuccessful = false;
			e.printStackTrace();
		}
		
		return isSuccessful;
	}

	@Override
	public boolean undo() {
		ServiceManager service = ServiceManager.getInstance();
		issue.addLabels(removedLabels);
		try {
			ArrayList<Label> ghLabels = CollectionUtilities.getGithubLabelList(issue.getLabels());
			service.setLabelsForIssue(issue.getId(), ghLabels);
			updateGithubIssueState();
			logRemoveOperation(false);
			isUndone = true;
		} catch (IOException e) {
			issue.removeLabels(removedLabels);
			isUndone = false;
			e.printStackTrace();
		}
		
		return isUndone;
	}

}
