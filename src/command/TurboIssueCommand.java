package command;

import java.io.IOException;
import java.lang.ref.WeakReference;

import service.ServiceManager;
import util.UserConfigurations;
import model.Model;
import model.TurboIssue;
import model.TurboLabel;

public abstract class TurboIssueCommand {
	protected TurboIssue issue;
	protected static final String LABELS_ADD_LOG_PREFIX = "Labels added: ";
	protected static final String LABELS_REMOVE_LOG_PREFIX = "Labels removed: ";
	protected static final String DESCRIPTION_CHANGE_LOG = "Edited description."; 
	protected WeakReference<Model> model;
	protected boolean isUndoableCommand = false;
	protected boolean isSuccessful = false;
	protected boolean isUndone = false;
	
	public TurboIssueCommand(Model model, TurboIssue issue){
		this.issue = issue;
		this.model = new WeakReference<Model>(model);
	}
	
	public boolean getIsUndoable(){
		return isUndoableCommand;
	}
	
	public abstract boolean execute();
	public abstract boolean undo();
	
	protected void updateGithubIssueState() throws IOException{
		ServiceManager service = ServiceManager.getInstance();
		if(issue.getOpen() == true){
			service.openIssue(issue.getId());
		}else{
			service.closeIssue(issue.getId());
		}
	}
	
	public void processInheritedLabels(Integer newParent, Integer originalParent) {
		removeInheritedLabel(originalParent, issue);
		addInheritedLabel(newParent, issue);
	}
	
	private void addInheritedLabel(Integer added, TurboIssue issue){
		TurboIssue addedParent = model.get().getIssueWithId(added);
		if(added == null){
			return;
		}
		
		for(TurboLabel label : addedParent.getLabels()){
			if(!UserConfigurations.isExcludedLabel(label.toGhName())){
				issue.addLabel(label);
			}
		}
	}
	
	private void removeInheritedLabel(Integer removed, TurboIssue issue){
		TurboIssue removedParent = model.get().getIssueWithId(removed);
		if(removedParent == null){
			return;
		}
		
		for (TurboLabel label : removedParent.getLabels()) {
			if(!UserConfigurations.isExcludedLabel(label.toGhName())){
				issue.removeLabel(label);
			}
		}
	}
}
