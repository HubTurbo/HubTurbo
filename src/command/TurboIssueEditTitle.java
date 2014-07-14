package command;

import java.io.IOException;

import service.ServiceManager;
import model.Model;
import model.TurboIssue;

/**
 * Updates issue title on github. 
 * Also sets the title of the given TurboIssue object to the given description String.
 * */

public class TurboIssueEditTitle extends TurboIssueCommand{
	
	private String newTitle;
	
	public TurboIssueEditTitle(Model model, TurboIssue issue, String title){
		super(model, issue);
		this.newTitle = title;
	}
	
	private void logTitleChange(String prevTitle, String newTitle){
		String changeLog = ("Title edited: [previous: " + newTitle + "] [new: " + prevTitle + "]\n");
		ServiceManager.getInstance().logIssueChanges(issue.getId(), changeLog);
	}

	@Override
	public boolean execute() {
		String previousTitle = issue.getTitle();
		try {
			ServiceManager.getInstance().editIssueTitle(issue.getId(), newTitle);
			issue.setTitle(newTitle);
			logTitleChange(previousTitle, newTitle);
			isSuccessful = true;
		} catch (IOException e) {
			isSuccessful = false;
			e.printStackTrace();
		}
		return isSuccessful;
	}

	@Override
	public boolean undo() {
		return true;
	}
}
