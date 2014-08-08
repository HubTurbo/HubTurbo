package ui.issuepanel.expanded.comments;

import handler.IssueDetailsContentHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import model.TurboComment;
import model.TurboIssue;

public class DetailsCell extends ListCell<TurboComment>{
	TurboIssue issue;
	IssueDetailsCard display;
	
	public DetailsCell(TurboIssue issue, IssueDetailsContentHandler handler){
		this.issue = issue;
		display = new CommentCard(handler);
		this.getStyleClass().add("comments-list-cell");
	}
	
	@Override
	public void updateItem(TurboComment item, boolean empty){
 		super.updateItem(item, empty);
 		setAlignment(Pos.CENTER);
 		if(item != null){
 			display.setDisplayedItem(item);
 			setGraphic(display);
 		}
	}
}
