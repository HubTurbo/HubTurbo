package ui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Model;
import model.TurboCollaborator;
import model.TurboIssue;
import model.TurboLabel;
import model.TurboMilestone;

public class IssueDialog implements Dialog<String> {

	private static final double HEIGHT_FACTOR = 0.3;

	private static final int TITLE_SPACING = 5;
	private static final int ELEMENT_SPACING = 10;
	private static final int MIDDLE_SPACING = 20;

	public static final String STYLE_YELLOW = "-fx-background-color: #FFFA73;";
	public static final String STYLE_BORDERS = "-fx-border-color: #000000; -fx-border-width: 1px;";

	Stage parentStage;
	Model model;
	TurboIssue issue;

	CompletableFuture<String> response;

	public IssueDialog(Stage parentStage, Model model, TurboIssue issue) {
		this.parentStage = parentStage;
		this.model = model;
		this.issue = issue;

		response = new CompletableFuture<>();
	}

	public CompletableFuture<String> show() {
		showDialog();
		return response;
	}

	private Parent left() {

		HBox title = new HBox();
		title.setAlignment(Pos.BASELINE_LEFT);
		title.setSpacing(TITLE_SPACING);

		Label issueId = new Label("#" + issue.getId());
		TextField issueTitle = new TextField(issue.getTitle());
		issueTitle.setPromptText("Title");
		issueTitle.textProperty().addListener(
				(observable, oldValue, newValue) -> {
					issue.setTitle(newValue);
				});
		
		CheckBox closedCheckBox = new CheckBox("Closed");
		closedCheckBox.setSelected(!issue.getOpen());
		closedCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
	        public void changed(ObservableValue<? extends Boolean> ov,
	            Boolean oldValue, Boolean newValue) {
	        	issue.setOpen(!newValue);
	        }
	    });
		
		title.getChildren().addAll(issueId, issueTitle, closedCheckBox);

		TextArea issueDesc = new TextArea(issue.getDescription());
		issueDesc.setPrefRowCount(5);
		issueDesc.setPrefColumnCount(42);
		issueDesc.setPromptText("Description");
		issueDesc.textProperty().addListener(
				(observable, oldValue, newValue) -> {
					issue.setDescription(newValue);
				});

		VBox left = new VBox();
		left.setSpacing(ELEMENT_SPACING);
		left.getChildren().addAll(title, issueDesc);

		return left;

	}

	private Parent right(Stage stage) {

		Parent milestoneField = createMilestoneBox(stage);
		Parent labelBox = createLabelBox(stage);
		Parent assigneeField = createAssigneeBox(stage);
		
		HBox buttons = createButtons(stage);

		VBox right = new VBox();
		right.setSpacing(ELEMENT_SPACING);
		right.getChildren().addAll(milestoneField, labelBox, assigneeField,
				buttons);

		return right;
	}

	private HBox createButtons(Stage stage) {
		HBox buttons = new HBox();
		buttons.setAlignment(Pos.BASELINE_RIGHT);

		Button cancel = new Button();
		cancel.setText("Cancel");
		cancel.setOnMouseClicked((MouseEvent e) -> {
			response.complete("cancel");
			stage.close();
		});

		Button ok = new Button();
		ok.setText("OK");
		ok.setOnMouseClicked((MouseEvent e) -> {
			response.complete("ok");
			stage.close();
		});
		HBox.setMargin(ok, new Insets(0, 12, 0, 0)); // top right bottom left

		buttons.getChildren().addAll(ok, cancel);
		return buttons;
	}

	private Parent createAssigneeBox(Stage stage) {
		
		final HBox assigneeBox = new HBox();
		assigneeBox.setStyle(UI.STYLE_BORDERS_FADED);
		
		Label label;
		if (issue.getAssignee() == null) {
			label = new Label("Assignee");
			label.setStyle(UI.STYLE_FADED + "-fx-padding: 5 5 5 5;");
		} else {
			label = new Label(issue.getAssignee().getGithubName());
			label.setStyle("-fx-padding: 5 5 5 5;");
		}
		assigneeBox.getChildren().add(label);
		
		List<TurboCollaborator> allAssignees = model.getCollaborators();
		
		assigneeBox.setOnMouseClicked((e) -> {
			
			ArrayList<Integer> existingIndices = new ArrayList<Integer>();
			if (issue.getAssignee() != null) {
				int existingIndex = -1;
				for (int i=0; i<allAssignees.size(); i++) {
					if (allAssignees.get(i).equals(issue.getAssignee())) {
						existingIndex = i;
					}
				}
				assert existingIndex != -1;
				existingIndices.add(existingIndex);
			}
			
			(new FilterableCheckboxList(stage, FXCollections
					.observableArrayList(allAssignees)))
					.setWindowTitle("Choose assignee")
					.setMultipleSelection(false)
					.setInitialCheckedState(existingIndices)
					.show()
					.thenApply((response) -> {
							boolean wasAnythingSelected = response.size() > 0;
							if (wasAnythingSelected) {
								TurboCollaborator assignee = allAssignees.get(response.get(0));
								
								// We don't have data binding for this box; set it manually
								label.setText(assignee.getGithubName());
								
								issue.setAssignee(assignee);
							} else {
								
								// Again, no data binding
								label.setText("Assignee");
								label.setStyle(UI.STYLE_FADED + "-fx-padding: 5 5 5 5;");

								issue.setAssignee(null);
							}
							return true;
						});
		});
		return assigneeBox;
	}

	private LabelDisplayBox createLabelBox(Stage stage) {
		final LabelDisplayBox labelBox = new LabelDisplayBox(issue.getLabels());
		labelBox.setStyle(UI.STYLE_BORDERS_FADED);
		
		if (issue.getLabels().size() == 0) {
			Label noLabels = new Label("Labels");
			noLabels.setStyle(UI.STYLE_FADED + "-fx-padding: 5 5 5 5;");
			labelBox.getChildren().add(noLabels);
		}
		
		List<TurboLabel> allLabels = model.getLabels();
		
		labelBox.setOnMouseClicked((e) -> {
			List<Integer> indicesForExistingLabels = issue.getLabels().stream()
					.map((label) -> {
						for (int i = 0; i < allLabels.size(); i++) {
							if (allLabels.get(i).equals(label)) {
								return i;
							}
						}
						assert false;
						return -1;
					}).collect(Collectors.toList());

			(new FilterableCheckboxList(stage, FXCollections
					.observableArrayList(allLabels)))
					.setWindowTitle("Choose labels")
					.setMultipleSelection(true)
					.setInitialCheckedState(indicesForExistingLabels)
					.show()
					.thenApply(
							(List<Integer> response) -> {
								List<TurboLabel> labels = response.stream()
										.map((i) -> allLabels.get(i))
										.collect(Collectors.toList());
								issue.setLabels(FXCollections
										.observableArrayList(labels));
								return true;
							});
		});
		return labelBox;
	}

	private Parent createMilestoneBox(Stage stage) {
		
		final HBox milestoneBox = new HBox();
		milestoneBox.setStyle(UI.STYLE_BORDERS_FADED);
		
		Label label;
		if (issue.getMilestone() == null) {
			label = new Label("Milestone");
			label.setStyle(UI.STYLE_FADED + "-fx-padding: 5 5 5 5;");
		} else {
			label = new Label(issue.getMilestone().getTitle());
			label.setStyle("-fx-padding: 5 5 5 5;");
		}
		milestoneBox.getChildren().add(label);
		
		List<TurboMilestone> allMilestones = model.getMilestones();
		
		milestoneBox.setOnMouseClicked((e) -> {
			
			ArrayList<Integer> existingIndices = new ArrayList<Integer>();
			if (issue.getMilestone() != null) {
				int existingIndex = -1;
				for (int i=0; i<allMilestones.size(); i++) {
					if (allMilestones.get(i).equals(issue.getMilestone())) {
						existingIndex = i;
					}
				}
				assert existingIndex != -1;
				existingIndices.add(existingIndex);
			}
			
			(new FilterableCheckboxList(stage, FXCollections
					.observableArrayList(allMilestones)))
					.setWindowTitle("Choose milestone")
					.setMultipleSelection(false)
					.setInitialCheckedState(existingIndices)
					.show()
					.thenApply((response) -> {
							boolean wasAnythingSelected = response.size() > 0;
							if (wasAnythingSelected) {
								TurboMilestone milestone = allMilestones.get(response.get(0));
								
								// We don't have data binding for this box; set it manually
								label.setText(milestone.getTitle());
								
								issue.setMilestone(milestone);
							} else {
								
								// Again, no data binding
								label.setText("Milestone");
								label.setStyle(UI.STYLE_FADED + "-fx-padding: 5 5 5 5;");

								issue.setMilestone(null);
							}
							return true;
						});
		});
		return milestoneBox;
	}

	private void showDialog() {

		HBox layout = new HBox();
		layout.setPadding(new Insets(15));
		layout.setSpacing(MIDDLE_SPACING);

		Scene scene = new Scene(layout, parentStage.getWidth(),
				parentStage.getHeight() * HEIGHT_FACTOR);

		Stage stage = new Stage();
		stage.setTitle("Issue #" + issue.getId() + ": " + issue.getTitle());
		stage.setScene(scene);

		Platform.runLater(() -> stage.requestFocus());

		layout.getChildren().addAll(left(), right(stage));

		stage.initOwner(parentStage);
		// secondStage.initModality(Modality.APPLICATION_MODAL);

		stage.setX(parentStage.getX());
		stage.setY(parentStage.getY() + parentStage.getHeight()
				* (1 - HEIGHT_FACTOR));

		stage.show();
	}
}
