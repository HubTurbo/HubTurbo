package command;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.egit.github.core.Label;

import service.ServiceManager;
import util.CollectionUtilities;
import model.Model;
import model.TurboIssue;
import model.TurboLabel;

public class TurboIssueRemoveLabels extends TurboIssueCommand{
	
	private List<TurboLabel> removedLabels;
	
	public TurboIssueRemoveLabels(Model model, TurboIssue issue, List<TurboLabel> labels){
		super(model, issue);
		this.removedLabels = labels;
	}

	@Override
	public boolean execute() {
		ServiceManager service = ServiceManager.getInstance();
		ArrayList<Label> ghLabels = CollectionUtilities.getGithubLabelList(removedLabels);
		issue.removeLabels(removedLabels);
		try {
			service.deleteLabelsFromIssue(issue.getId(), ghLabels);
			updateGithubIssueState();
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
		ArrayList<Label> ghLabels = CollectionUtilities.getGithubLabelList(removedLabels);
		issue.addLabels(removedLabels);
		try {
			service.addLabelsToIssue(issue.getId(), ghLabels);
			updateGithubIssueState();
			isUndone = true;
		} catch (IOException e) {
			issue.removeLabels(removedLabels);
			isUndone = false;
			e.printStackTrace();
		}
		
		return isUndone;
	}

}
