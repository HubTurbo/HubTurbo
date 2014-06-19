package ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
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

		Text issueName = new Text("#" + issue.getId() + " " + issue.getTitle());
		issueName.setStyle(STYLE_ISSUE_NAME);
		issue.titleProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(
					ObservableValue<? extends String> stringProperty,
					String oldValue, String newValue) {
				issueName.setText("#" + issue.getId() + " " + newValue);
			}
		});

		Text parentName = new Text("parent");
		parentName.setStyle(STYLE_PARENT_NAME);

		HBox labels = new HBox();
		labels.setSpacing(3);
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

		VBox everything = new VBox();
		everything.setSpacing(2);
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
			return true;
		});
	}

	private void populateLabels(HBox parent, ObservableList<TurboLabel> labels) {
		for (TurboLabel label : labels) {
			Label labelText = new Label(label.getName());
			labelText.setStyle(getStyleFor(label));
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

	private String getStyleFor(TurboLabel label) {
		String colour = "slateblue";
		if (label.getName().equals("bug")) {
			colour = "red";
		} else if (label.getName().equals("feature")) {
			colour = "green";
		}
		String style = "-fx-background-color: " + colour + "; -fx-text-fill: white; -fx-background-radius: 5; -fx-border-radius: 20; -fx-padding: 3;";
		return style;
	}
}
