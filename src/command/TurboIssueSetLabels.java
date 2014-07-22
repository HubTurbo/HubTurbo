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
 * Sets labels of issue on github
 * Also sets labels of given TurboIssue object to given list of TurboLabels
 * */

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
		isSuccessful = setLabelsForIssue(previousLabels, newLabels, true);
		return isSuccessful;
	}
	
	private void setGithubLabelsForIssue(List<Label> ghLabels) throws IOException{
		ServiceManager.getInstance().setLabelsForIssue(issue.getId(), ghLabels);
		updateGithubIssueState();
	}
	
	private boolean setLabelsForIssue(List<TurboLabel> oldLabels, List<TurboLabel>updatedLabels, boolean logRemarks){
		issue.setLabels(updatedLabels);
		ArrayList<Label> ghLabels = CollectionUtilities.getGithubLabelList(updatedLabels);
		try {
			setGithubLabelsForIssue(ghLabels);
			logLabelsChange(oldLabels, updatedLabels, logRemarks);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			issue.setLabels(oldLabels);
			return false;
		}
	}
	
	private void logLabelsChange(List<TurboLabel> oldLabels, List<TurboLabel> labels, boolean logRemarks){		
		lastOperationExecuted = IssueChangeLogger.logLabelsChange(model.get(), issue, oldLabels, labels);
	}
	
	@Override
	public boolean undo() {
		if(isSuccessful){
			isUndone = setLabelsForIssue(newLabels, previousLabels, false);
		}
		return isUndone;
	}

}
