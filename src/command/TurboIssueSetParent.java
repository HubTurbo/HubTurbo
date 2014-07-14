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
	
	private void logParentChange(Integer parent, Integer oldParent){
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
	
	private void setLocalIssueParent(Integer parent, Integer oldParent){
		issue.setParentIssue(parent);
		processInheritedLabels(parent, oldParent);
	}
	
	private void updateGithubIssueParent(Integer parent, Integer oldParent) throws IOException{
		ServiceManager service = ServiceManager.getInstance();
		service.editIssueBody(issue.getId(), issue.buildGithubBody());
		ArrayList<Label> ghLabels = CollectionUtilities.getGithubLabelList(issue.getLabels());
		service.setLabelsForIssue(issue.getId(), ghLabels);
	}
	
	private boolean setIssueParent(Integer parent, Integer oldParent){
		setLocalIssueParent(parent, oldParent);
		try {
			updateGithubIssueParent(parent, oldParent);
			logParentChange(parent, oldParent);
			return true;
		} catch (IOException e) {
			setLocalIssueParent(oldParent, parent);
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public boolean execute() {
		isSuccessful = setIssueParent(newParent, previousParent);
		return isSuccessful;
	}

	@Override
	public boolean undo() {
		isUndone = setIssueParent(previousParent, newParent);
		return isUndone;
	}

}
