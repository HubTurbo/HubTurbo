package command;

import java.io.IOException;
import java.lang.ref.WeakReference;

import service.ServiceManager;
import model.Model;
import model.TurboIssue;

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
}
