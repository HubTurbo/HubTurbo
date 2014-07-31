package command;

import java.io.IOException;
import java.lang.ref.WeakReference;

import javafx.application.Platform;
import service.ServiceManager;
import util.ProjectConfigurations;
import model.Model;
import model.TurboIssue;
import model.TurboLabel;

public abstract class TurboIssueCommand {
	protected TurboIssue issue;

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
	
	public boolean execute(){
		boolean result = performExecuteAction();
		if(result){
			Platform.runLater(() -> {
				model.get().refresh();
			});
		}
		return result;
	}
	protected abstract boolean performExecuteAction();
	
	public boolean undo(){
		boolean result = performUndoAction();
		if(result){
			Platform.runLater(() -> {
				model.get().refresh();
			});
		}
		return result;
	}
	protected abstract boolean performUndoAction();
	
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
			if(!ProjectConfigurations.isNonInheritedLabel(label.toGhName())){
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
			if(!ProjectConfigurations.isNonInheritedLabel(label.toGhName())){
				issue.removeLabel(label);
			}
		}
	}
}
