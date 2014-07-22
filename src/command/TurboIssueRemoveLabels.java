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
	
	private void logRemoveOperation(List<TurboLabel> original, List<TurboLabel> edited){
		String changeLog = IssueChangeLogger.logLabelsChange(model.get(), issue, original, edited);
		lastOperationExecuted = changeLog;
	}
	

	@Override
	public boolean execute() {
		ServiceManager service = ServiceManager.getInstance();
		List<TurboLabel> original = issue.getLabels();
		ArrayList<Label> ghLabels = CollectionUtilities.getGithubLabelList(removedLabels);
		issue.removeLabels(removedLabels);
		try {
			service.deleteLabelsFromIssue(issue.getId(), ghLabels);
			updateGithubIssueState();
			logRemoveOperation(original, issue.getLabels());
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
		List<TurboLabel> original = issue.getLabels();
		issue.addLabels(removedLabels);
		try {
			ArrayList<Label> ghLabels = CollectionUtilities.getGithubLabelList(issue.getLabels());
			service.setLabelsForIssue(issue.getId(), ghLabels);
			updateGithubIssueState();
			logRemoveOperation(original, issue.getLabels());
			isUndone = true;
		} catch (IOException e) {
			issue.removeLabels(removedLabels);
			isUndone = false;
			e.printStackTrace();
		}
		
		return isUndone;
	}

}
