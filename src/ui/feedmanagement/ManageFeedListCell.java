package ui.feedmanagement;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import model.TurboFeed;

import ui.DragData;

public class ManageFeedListCell extends ListCell<TurboFeed>{

	@Override
	protected void updateItem(TurboFeed feed, boolean empty) {
		super.updateItem(feed, empty);
		if (feed == null) {
			return;
		}
		setGraphic(createFeedItem(feed));
		
		setOnDragDetected((event) -> {
			Dragboard db = startDragAndDrop(TransferMode.COPY);
			ClipboardContent content = new ClipboardContent();
			DragData dd = new DragData(DragData.Source.FEED_TAB, feed.getListName());
			content.putString(dd.serialise());
			db.setContent(content);
			event.consume();
		});
		
		setOnDragDone((event) -> {
			event.consume();
		});
	}
	
	private HBox createFeedItem(TurboFeed feed) {
		Label feedHandle = new Label(feed.getListName());
		feedHandle.getStyleClass().add("display-box-padding");

		HBox feedItem = new HBox();
		feedItem.setSpacing(5);
		feedItem.setAlignment(Pos.BASELINE_LEFT);
		feedItem.getChildren().addAll(feedHandle);
		
		return feedItem;
	}
}
