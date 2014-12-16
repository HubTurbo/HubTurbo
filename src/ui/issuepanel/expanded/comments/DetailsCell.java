package ui.issuepanel.expanded.comments;

import handler.IssueDetailsContentHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import model.TurboComment;
import model.TurboIssue;
import service.TurboIssueEvent;

public class DetailsCell extends ListCell<CommentListItem>{
	TurboIssue issue;
	IssueDetailsCard commentDisplay;
	IssueEventsCard eventDisplay;
	
	public DetailsCell(TurboIssue issue, IssueDetailsContentHandler handler){
		this.issue = issue;
		commentDisplay = new CommentCard(handler);
		this.getStyleClass().add("comments-list-cell");
	}
	
	@Override
	public void updateItem(CommentListItem item, boolean empty){
 		if(item != null){
 	 		super.updateItem(item, empty);
 	 		setAlignment(Pos.CENTER);
 			if (item instanceof TurboComment) {
 	 			commentDisplay.setDisplayedItem((TurboComment) item);
 	 			setGraphic(commentDisplay);
 			} else {
 				assert item instanceof TurboIssueEvent;
 				eventDisplay = new IssueEventsCard((TurboIssueEvent) item);
 	 			setGraphic(eventDisplay);
 			}
 		}
	}
}
