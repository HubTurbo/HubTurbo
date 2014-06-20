package ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import logic.LogicFacade;
import logic.TurboCollaborator;
import logic.TurboIssue;

public class CustomListCell extends ListCell<TurboIssue> {

	private final Stage mainStage;
	private final LogicFacade logic;
	private final IssuePanel parentIssuePanel;

	private static final String STYLE_PARENT_NAME = "-fx-font-size: 11px;";
	private static final String STYLE_ISSUE_NAME = "-fx-font-size: 24px;";

	public CustomListCell(Stage mainStage, LogicFacade logic, IssuePanel parent) {
		super();
		this.mainStage = mainStage;
		this.logic = logic;
		this.parentIssuePanel = parent;
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

		LabelDisplayBox labels = new LabelDisplayBox(issue.getLabels());

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
		(new IssueDialog(mainStage, logic, issue)).show().thenApply(newIssue -> {
			// Perform a manual refresh in case anything was changed
			parentIssuePanel.refreshItems();
			return true;
		});
	}
}
