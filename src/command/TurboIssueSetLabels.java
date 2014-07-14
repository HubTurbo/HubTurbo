package command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.egit.github.core.Label;

import service.ServiceManager;
import util.CollectionUtilities;
import model.Model;
import model.TurboIssue;
import model.TurboLabel;

public class TurboIssueSetLabels extends TurboIssueCommand{
	private List<TurboLabel> previousLabels;
	private List<TurboLabel> newLabels;
	
	public TurboIssueSetLabels(Model model, TurboIssue issue, List<TurboLabel> labels){
		super(model, issue);
		this.newLabels = labels;
		this.previousLabels = issue.getLabels(); //Is a copy of original list of labels
	}
	
	@Override
	public boolean execute() {
		isSuccessful = setLabelsForIssue(newLabels, previousLabels);
		if(isSuccessful){
			logLabelsChange(newLabels, previousLabels);
		}
		return isSuccessful;
	}
	
	private boolean setLabelsForIssue(List<TurboLabel>labels, List<TurboLabel> oldLabels){
		ServiceManager service = ServiceManager.getInstance();
		issue.setLabels(labels);
		ArrayList<Label> ghLabels = CollectionUtilities.getGithubLabelList(labels);
		try {
			service.setLabelsForIssue(issue.getId(), ghLabels);
			updateGithubIssueState();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			issue.setLabels(oldLabels);
			return false;
		}
	}
	
	
	private void logLabelsChange(List<TurboLabel> labels, List<TurboLabel> oldLabels){
		HashMap<String, HashSet<TurboLabel>> changes = CollectionUtilities.getChangesToList(oldLabels, labels);
		HashSet<TurboLabel> removed = changes.get(CollectionUtilities.REMOVED_TAG);
		HashSet<TurboLabel> added = changes.get(CollectionUtilities.ADDED_TAG);
		StringBuilder changeLog = new StringBuilder();
		if(added.size() > 0){
			changeLog.append(LABELS_ADD_LOG_PREFIX + added.toString() + "\n");
		}
		if(removed.size() > 0){
			changeLog.append(LABELS_REMOVE_LOG_PREFIX + removed.toString() + "\n");
		}
		ServiceManager.getInstance().logIssueChanges(issue.getId(), changeLog.toString());
	}
	
	@Override
	public boolean undo() {
		if(isSuccessful){
			isUndone = setLabelsForIssue(previousLabels, newLabels);
		}
		if(isUndone){
			logLabelsChange(previousLabels, newLabels);
		}
		return isUndone;
	}

}
