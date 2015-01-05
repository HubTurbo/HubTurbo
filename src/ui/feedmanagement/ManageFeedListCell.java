package ui.feedmanagement;

import org.eclipse.egit.github.core.User;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import model.Model;
import model.TurboFeed;
import model.TurboIssue;
import model.TurboLabel;
import model.TurboUser;
import ui.DragData;

public class ManageFeedListCell extends ListCell<TurboFeed> {
	private final Model model;
	private static final int CARD_WIDTH = 350;
	private FlowPane labelDetails = new FlowPane();

	public ManageFeedListCell(Model model) {
		this.model = model;
	}

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
			DragData dd = new DragData(DragData.Source.FEED_TAB, feed.getFeed());
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
