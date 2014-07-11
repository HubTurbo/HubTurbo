package command;

import java.io.IOException;
import java.lang.ref.WeakReference;
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
		this.issue = issue;
		this.model = new WeakReference<Model>(model);
		this.newLabels = labels;
	}
	
	@Override
	public boolean execute() {
		
		this.previousLabels = issue.getLabels(); //Is a copy of original list of labels
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
			if(issue.getOpen() == true){
				service.openIssue(issue.getId());
			}else{
				service.closeIssue(issue.getId());
			}
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
		String changeLog = "Labels added: " + added.toString() + "\n Labels Removed: " + removed.toString();
		ServiceManager.getInstance().logIssueChanges(issue.getId(), changeLog);
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
