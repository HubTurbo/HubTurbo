package ui;

import javafx.scene.control.ListCell;
import model.TurboComment;
import model.TurboIssue;

import org.eclipse.egit.github.core.Comment;

import ui.IssueDetailsDisplay.DisplayType;


public class DetailsCell extends ListCell<TurboComment>{
	TurboIssue issue;
	DisplayType displayType;
	
	public DetailsCell(TurboIssue issue, DisplayType displayType){
		this.issue = issue;
	}
	
	@Override
	public void updateItem(TurboComment item, boolean empty){
		super.updateItem(item, empty);
		
	}
}
