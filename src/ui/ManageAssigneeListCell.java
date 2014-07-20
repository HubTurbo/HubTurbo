package ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
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
	}
	
	private HBox createAssigneeItem(TurboUser user) {
		ImageView avatar = new ImageView(user.getAvatar());
		
		Label assigneeHandle = new Label((user.getGithubName()));
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
