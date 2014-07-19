package ui;

import handler.IssueDetailsContentHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import model.TurboComment;
import model.TurboIssue;
import ui.IssueDetailsDisplay.DisplayType;


public class DetailsCell extends ListCell<TurboComment>{
	TurboIssue issue;
	DisplayType displayType;
	IssueDetailsCard display;
	
	public DetailsCell(TurboIssue issue, DisplayType displayType, IssueDetailsContentHandler handler){
		this.issue = issue;
		this.displayType = displayType;
		if(displayType == DisplayType.COMMENTS){
			display = new CommentCard(handler);
		}else{
			display = new IssueDetailsCard();
		}
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
