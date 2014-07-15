package command;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.egit.github.core.Label;

import service.ServiceManager;
import util.CollectionUtilities;
import model.Model;
import model.TurboIssue;

/**
 * Updates issue parent on github. 
 * Also sets the parent of the given TurboIssue object to the given parent issue number
 * */

public class TurboIssueSetParent extends TurboIssueCommand{
	private Integer newParent;
	private Integer previousParent;
	
	public TurboIssueSetParent(Model model, TurboIssue issue, Integer parent){
		super(model, issue);
		newParent = parent;
		previousParent = issue.getParentIssue();
	}
	
	private void logParentChange(Integer oldParent, Integer parent, boolean logRemarks){
		String changeLog;
		if(parent < 0){
			changeLog = String.format("Removed issue parent: %1d\n", oldParent);
		}else if(oldParent > 0){
			changeLog = String.format("Changed Issue parent from %1d to %2d\n", oldParent, parent);
		}else{
			changeLog = String.format("Set Issue parent to %1d\n", parent);
		}
		lastOperationExecuted = changeLog;
		logChangesInGithub(logRemarks, changeLog);
	}
	
	private void setLocalIssueParent(Integer oldParent, Integer parent){
		issue.setParentIssue(parent);
		processInheritedLabels(oldParent, parent, issue);
	}
	
	private void updateGithubIssueParent(Integer oldParent, Integer parent) throws IOException{
		ServiceManager service = ServiceManager.getInstance();
		service.editIssueBody(issue.getId(), issue.buildGithubBody());
		ArrayList<Label> ghLabels = CollectionUtilities.getGithubLabelList(issue.getLabels());
		service.setLabelsForIssue(issue.getId(), ghLabels);
	}
	
	private boolean setIssueParent(Integer oldParent, Integer parent, boolean logRemarks){
		setLocalIssueParent(oldParent, parent);
		try {
			updateGithubIssueParent(oldParent, parent);
			logParentChange(oldParent, parent, logRemarks);
			return true;
		} catch (IOException e) {
			setLocalIssueParent(parent, oldParent);
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public boolean execute() {
		isSuccessful = setIssueParent(previousParent, newParent, true);
		return isSuccessful;
	}

	@Override
	public boolean undo() {
		isUndone = setIssueParent(newParent, previousParent, false);
		return isUndone;
	}

}
