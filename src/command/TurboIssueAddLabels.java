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

public class TurboIssueAddLabels extends TurboIssueCommand{
	
	private List<TurboLabel> addedLabels;
	
	public TurboIssueAddLabels(Model model, TurboIssue issue, List<TurboLabel> labels){
		super(model, issue);
		this.addedLabels = labels;
	}
	
	private void logAddOperation(boolean added){
		String changeLog;
		if(added){
			changeLog = LABELS_ADD_LOG_PREFIX + addedLabels.toString();
		}else{
			changeLog = LABELS_REMOVE_LOG_PREFIX + addedLabels.toString();
		}
		ServiceManager.getInstance().logIssueChanges(issue.getId(), changeLog);
	}

	@Override
	public boolean execute() {
		ServiceManager service = ServiceManager.getInstance();
		ArrayList<Label> ghLabels = CollectionUtilities.getGithubLabelList(addedLabels);
		issue.addLabels(addedLabels);
		try {
			service.addLabelsToIssue(issue.getId(), ghLabels);
			updateGithubIssueState();
			logAddOperation(true);
			isSuccessful = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			issue.removeLabels(addedLabels);
			isSuccessful = false;
			e.printStackTrace();
		}
		
		return isSuccessful;
	}

	@Override
	public boolean undo() {
		ServiceManager service = ServiceManager.getInstance();
		ArrayList<Label> ghLabels = CollectionUtilities.getGithubLabelList(addedLabels);
		issue.removeLabels(addedLabels);
		try {
			service.deleteLabelsFromIssue(issue.getId(), ghLabels);
			updateGithubIssueState();
			logAddOperation(false);
			isUndone = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			issue.addLabels(addedLabels);
			e.printStackTrace();
			isUndone = false;
		}
		return isUndone;
	}

}
