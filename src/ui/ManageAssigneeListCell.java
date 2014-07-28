package ui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import model.TurboUser;

public class ManageAssigneeListCell extends ListCell<TurboUser> {

	@Override
	protected void updateItem(TurboUser user, boolean empty) {
		super.updateItem(user, empty);
		if (user == null) {
			return;
		}
		setGraphic(createAssigneeItem(user));
		
		setOnDragDetected((event) -> {
			Dragboard db = startDragAndDrop(TransferMode.COPY);
			ClipboardContent content = new ClipboardContent();
			DragData dd = new DragData(DragData.Source.ASSIGNEE_TAB, user.getGithubName());
			content.putString(dd.serialise());
			db.setContent(content);
			event.consume();
		});
		
		setOnDragDone((event) -> {
			event.consume();
		});
	}
	
	private HBox createAssigneeItem(TurboUser user) {
		ImageView avatar = new ImageView(user.getAvatar());
		
		Label assigneeHandle = new Label((user.getAlias()));
		assigneeHandle.getStyleClass().add("display-box-padding");

		HBox assigneeItem = new HBox();
		assigneeItem.setSpacing(5);
		assigneeItem.setAlignment(Pos.BASELINE_LEFT);
		assigneeItem.getChildren().addAll(avatar, assigneeHandle);
		
		if (user.getRealName() != null) {
			Label assigneeName = new Label("(" + user.getRealName() + ")");
			assigneeName.getStyleClass().add("display-box-padding");
			assigneeItem.getChildren().add(assigneeName);
		}
		
		return assigneeItem;
	}
	
}
