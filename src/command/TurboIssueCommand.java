package command;

import java.io.IOException;
import java.lang.ref.WeakReference;

import javafx.application.Platform;
import model.Model;
import model.TurboIssue;
import model.TurboLabel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import service.ServiceManager;
import storage.DataManager;

public abstract class TurboIssueCommand {
	protected static final Logger logger = LogManager.getLogger(TurboIssueCommand.class.getName());
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
		if(issue.isOpen() == true){
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
			if(!DataManager.getInstance().isNonInheritedLabel(label.toGhName())){
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
			if(!DataManager.getInstance().isNonInheritedLabel(label.toGhName())){
				issue.removeLabel(label);
			}
		}
	}
}
