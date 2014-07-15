package command;

import java.util.ArrayList;
import java.util.List;

import model.Model;
import model.TurboIssue;
import model.TurboLabel;
import model.TurboMilestone;
import model.TurboUser;

public class TurboCommandExecutor {
	private ArrayList<String> executionLog;
	
	public TurboCommandExecutor(){
		executionLog = new ArrayList<String>();
	}
	
	public boolean executeCommmand(CommandType command, Model model, TurboIssue issue, Object... args){
		TurboIssueCommand issueCommand = createIssueCommand(command, model, issue, args);
		boolean result = issueCommand.execute();
		if(result){
			executionLog.add(issueCommand.getLastOperation());
		}
		return result;
	}
	
	protected TurboIssueCommand createIssueCommand(CommandType command, Model model, TurboIssue issue, Object... args){
		switch(command){
		case ADD_ISSUE:
			return new TurboIssueAdd(model, issue);
		case EDIT_ISSUE:
			if(args.length == 1 && args[0].getClass() == TurboIssue.class){
				return new TurboIssueEdit(model, issue, (TurboIssue)args[0]);
			}
		case ADD_LABELS: 
			if(args.length == 1){
				return new TurboIssueAddLabels(model, issue, (List<TurboLabel>)args[0]);
			}
		case SET_LABELS: 
			if(args.length == 1){
				return new TurboIssueSetLabels(model, issue, (List<TurboLabel>)args[0]);
			}
		case REMOVE_LABELS:
			if(args.length == 1){
				return new TurboIssueRemoveLabels(model, issue, (List<TurboLabel>)args[0]);
			}
		case SET_ASSIGNEE: 
			if(args.length == 1 && args[0].getClass() == TurboUser.class){
				return new TurboIssueSetAssignee(model, issue, (TurboUser)args[0]);
			}
		case SET_MILESTONE: 
			if(args.length == 1 && args[0].getClass() == TurboMilestone.class){
				return new TurboIssueSetMilestone(model, issue, (TurboMilestone)args[0]);
			}
		case SET_PARENT: 
			if(args.length == 1 && args[0].getClass() == Integer.class){
				return new TurboIssueSetParent(model, issue, (Integer)args[0]);
			}
		case EDIT_DESCRIPTION:
			if(args.length == 1 && args[0].getClass() == String.class){
				return new TurboIssueEditDescription(model, issue, (String)args[0]);
			}
		case EDIT_TITLE:
			if(args.length == 1 && args[0].getClass() == String.class){
				return new TurboIssueEditTitle(model, issue, (String)args[0]);
			}
		default:
			throw new IllegalArgumentException(); //TODO:
		}
	}
	
	
}
