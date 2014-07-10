package command;

import java.lang.ref.WeakReference;

import model.Model;
import model.TurboIssue;

public abstract class TurboIssueCommand {
	protected TurboIssue issue;
	protected WeakReference<Model> model;
	protected boolean isUndoableCommand;
	
	public boolean getIsUndoable(){
		return isUndoableCommand;
	}
	
	public abstract boolean execute();
	public abstract boolean undo();
}
