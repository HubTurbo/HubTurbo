package ui;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.Model;
import model.TurboIssue;
import model.TurboMilestone;

public class ManageMilestonesListCell extends ListCell<TurboMilestone> {
	private final Stage mainStage;
	private final Model model;
	
	public ManageMilestonesListCell(Stage mainStage, Model model) {
		super();
		this.mainStage = mainStage;
		this.model = model;
	}

	@Override
	public void updateItem(TurboMilestone milestone, boolean empty) {
		super.updateItem(milestone, empty);
		if (milestone == null)
			return;

		Text milestoneTitle = new Text(milestone.getTitle());
//		milestone.titleProperty().addListener(new ChangeListener<String>() {
//			@Override
//			public void changed(
//					ObservableValue<? extends String> stringProperty,
//					String oldValue, String newValue) {
//				milestoneTitle.setText("#" + milestone.getId() + " " + newValue);
//			}
//		});
//
//		Text parentName = new Text("parent");
//		parentName.setStyle(STYLE_PARENT_NAME);
//
//		LabelDisplayBox labels = new LabelDisplayBox(milestone.getLabels());
//
//		HBox assignee = new HBox();
//		assignee.setSpacing(3);
//		Text assignedToLabel = new Text("Assigned to:");
//		TurboCollaborator collaborator = milestone.getAssignee();
//		Text assigneeName = new Text(collaborator == null ? "none"
//				: collaborator.getGithubName());
//		assignee.getChildren().addAll(assignedToLabel, assigneeName);
//
		VBox everything = new VBox();
//		everything.setSpacing(2);
		everything.getChildren()
				.addAll(milestoneTitle);//, parentName, labels, assignee);
		// everything.getChildren().stream().forEach((node) ->
		// node.setStyle(Demo.STYLE_BORDERS));

		setGraphic(everything);

//		registerEvents(milestone);
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
		TurboIssue copy = new TurboIssue(issue);
		(new IssueDialog(mainStage, model, issue)).show().thenApply(
				response -> {
					if (response.equals("ok")) {
						model.updateIssue(copy, issue);
					}
					return true;
				});
	}
}
