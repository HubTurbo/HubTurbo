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
	protected static final String DESCRIPTION_CHANGE_LOG = "Edited description. \n"; 
	protected static final String ADDITIONAL_COMMENTS_FORMAT = "\n [Remarks] %1s \n";
	
	protected WeakReference<Model> model;
	protected boolean isUndoableCommand = false;
	protected boolean isSuccessful = false;
	protected boolean isUndone = false;
	protected String lastOperationExecuted = "";
	protected String loggingRemarks;
	
	public TurboIssueCommand(Model model, TurboIssue issue){
		this.issue = issue;
		this.model = new WeakReference<Model>(model);
	}
	
	public boolean getIsUndoable(){
		return isUndoableCommand;
	}
	
	public abstract boolean execute();
	public abstract boolean undo();
	
	public void setLoggingRemarks(String remarks){
		this.loggingRemarks = remarks;
	}
	
	public String getLastOperation(){
		return lastOperationExecuted;
	}
	
	protected void updateGithubIssueState() throws IOException{
		ServiceManager service = ServiceManager.getInstance();
		if(issue.getOpen() == true){
			service.openIssue(issue.getId());
		}else{
			service.closeIssue(issue.getId());
		}
	}
	
	protected void logChangesInGithub(boolean logAdditionalRemarks, String changeLog){
		if(logAdditionalRemarks && loggingRemarks != null && loggingRemarks.length() > 0){
			changeLog += String.format(ADDITIONAL_COMMENTS_FORMAT, loggingRemarks);
		}
		ServiceManager.getInstance().logIssueChanges(issue.getId(), changeLog);
	}
	
	protected void processInheritedLabels(Integer originalParent, Integer newParent, TurboIssue issue) {
		removeInheritedLabel(originalParent, issue);
		addInheritedLabel(newParent, issue);
	}
	
	private void addInheritedLabel(Integer added, TurboIssue issue){
		TurboIssue addedParent = model.get().getIssueWithId(added);
		if(addedParent == null){
			return;
		}
		
		for(TurboLabel label : addedParent.getLabels()){
			if(!UserConfigurations.isNonInheritedLabel(label.toGhName())){
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
			if(!UserConfigurations.isNonInheritedLabel(label.toGhName())){
				issue.removeLabel(label);
			}
		}
	}
}
