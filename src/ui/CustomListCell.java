package ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import logic.TurboCollaborator;
import logic.TurboIssue;
import logic.TurboLabel;

public class CustomListCell extends ListCell<TurboIssue> {

	private Stage mainStage;

	private static final String STYLE_PARENT_NAME = "-fx-font-size: 11px;";
	private static final String STYLE_ISSUE_NAME = "-fx-font-size: 24px;";

	public CustomListCell(Stage mainStage) {
		super();
		this.mainStage = mainStage;
	}

	@Override
	public void updateItem(TurboIssue issue, boolean empty) {
		super.updateItem(issue, empty);
		if (issue == null)
			return;

		VBox everything = new VBox();

		Text issueName = new Text(issue.getTitle());
		issueName.setStyle(STYLE_ISSUE_NAME);
		issue.titleProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(
					ObservableValue<? extends String> stringProperty,
					String oldValue, String newValue) {
				issueName.setText(newValue);
			}
		});

		Text parentName = new Text("parent");
		parentName.setStyle(STYLE_PARENT_NAME);

		HBox labels = new HBox();
		labels.setStyle(Demo.STYLE_BORDERS);
		issue.getLabels().addListener(new ListChangeListener<TurboLabel>() {

			@Override
			public void onChanged(
					javafx.collections.ListChangeListener.Change<? extends TurboLabel> arg0) {
				populateLabels(labels, issue.getLabels());
			}

		});
		populateLabels(labels, issue.getLabels());

		HBox assignee = new HBox();
		assignee.setSpacing(3);
		Text assignedToLabel = new Text("Assigned to:");
		TurboCollaborator collaborator = issue.getAssignee();
		Text assigneeName = new Text(collaborator == null ? "none"
				: collaborator.getGithubName());
		assignee.getChildren().addAll(assignedToLabel, assigneeName);

		everything.getChildren()
				.addAll(issueName, parentName, labels, assignee);
		// everything.getChildren().stream().forEach((node) ->
		// node.setStyle(Demo.STYLE_BORDERS));

		setGraphic(everything);

		registerEvents(issue);
	}

	private void registerEvents(TurboIssue issue) {
		setOnMouseClicked((MouseEvent mouseEvent) -> {
			if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
				if (mouseEvent.getClickCount() == 2) {
					onDoubleClick(issue);
				}
			}
		});
	}

	private void onDoubleClick(TurboIssue issue) {
		(new IssueDialog(mainStage, issue)).show().thenApply(newIssue -> {
//			System.out.println(msg);
//			if (msg.equals("ok")) {
//				System.out.println("YES");
//			}
			return true;
		});
	}

	private void populateLabels(HBox parent, ObservableList<TurboLabel> labels) {
		for (TurboLabel label : labels) {
			Text labelText = new Text(label.getName());
			label.nameProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(
						ObservableValue<? extends String> stringProperty,
						String oldValue, String newValue) {
					labelText.setText(newValue);
				}
			});
			parent.getChildren().add(labelText);
		}
	}
}
