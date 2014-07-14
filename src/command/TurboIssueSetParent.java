package command;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.egit.github.core.Label;

import service.ServiceManager;
import util.CollectionUtilities;
import model.Model;
import model.TurboIssue;

public class TurboIssueSetParent extends TurboIssueCommand{
	private Integer newParent;
	private Integer previousParent;
	
	public TurboIssueSetParent(Model model, TurboIssue issue, Integer parent){
		super(model, issue);
		newParent = parent;
		previousParent = issue.getParentIssue();
	}
	
	private void logParentChange(Integer oldParent, Integer parent){
		String changeLog;
		if(parent < 0){
			changeLog = String.format("Removed issue parent: %1d", oldParent);
		}else if(oldParent > 0){
			changeLog = String.format("Changed Issue parent from %1d to %2d", oldParent, parent);
		}else{
			changeLog = String.format("Set Issue parent to %1d", parent);
		}
		ServiceManager.getInstance().logIssueChanges(issue.getId(), changeLog);
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
	
	private boolean setIssueParent(Integer oldParent, Integer parent){
		setLocalIssueParent(oldParent, parent);
		try {
			updateGithubIssueParent(oldParent, parent);
			logParentChange(oldParent, parent);
			return true;
		} catch (IOException e) {
			setLocalIssueParent(parent, oldParent);
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public boolean execute() {
		isSuccessful = setIssueParent(previousParent, newParent);
		return isSuccessful;
	}

	@Override
	public boolean undo() {
		isUndone = setIssueParent(newParent, previousParent);
		return isUndone;
	}

}
