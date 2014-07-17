package ui;

import javafx.scene.control.ListCell;
import model.TurboIssue;

import org.eclipse.egit.github.core.Comment;

import ui.IssueDetailsDisplay.DisplayType;


public class DetailsCell extends ListCell<Comment>{
	TurboIssue issue;
	DisplayType displayType;
	
	public DetailsCell(TurboIssue issue, DisplayType displayType){
		this.issue = issue;
	}
	
	@Override
	public void updateItem(Comment item, boolean empty){
		super.updateItem(item, empty);
		
	}
}
