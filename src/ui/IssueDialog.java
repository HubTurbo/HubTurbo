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

	private Stage parentStage;
	private Model model;
	private TurboIssue issue;

	private CompletableFuture<String> response;

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
	
		layout.getChildren().addAll(left(stage), right(stage));
	
		stage.initOwner(parentStage);
		// secondStage.initModality(Modality.APPLICATION_MODAL);
	
		stage.setX(parentStage.getX());
		stage.setY(parentStage.getY() + parentStage.getHeight()
				* (1 - HEIGHT_FACTOR));
	
		stage.show();
	}

	private Parent left(Stage stage) {

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

		Parent parentsBox = createParentsBox(stage);
		
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
		left.getChildren().addAll(title, parentsBox, issueDesc);

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

	private Parent createParentsBox(Stage stage) {
		final ParentIssuesDisplayBox parentsBox = new ParentIssuesDisplayBox(issue.getParents(), true);
		List<TurboIssue> allIssues = model.getIssues();
		
		parentsBox.setOnMouseClicked((e) -> {
			List<Integer> indicesForExistingParents = issue.getParents().stream()
					.map((parent) -> {
						for (int i = 0; i < allIssues.size(); i++) {
							if (allIssues.get(i).getId() == parent) {
								return i;
							}
						}
						assert false;
						return -1;
					}).collect(Collectors.toList());

			(new FilterableCheckboxList(stage, FXCollections
					.observableArrayList(allIssues)))
					.setWindowTitle("Choose Parents")
					.setMultipleSelection(true)
					.setInitialCheckedState(indicesForExistingParents)
					.show()
					.thenApply(
							(List<Integer> response) -> {
								
								boolean wasAnythingSelected = response.size() > 0;
								if (wasAnythingSelected) {
									List<Integer> parents = response.stream()
											.map((i) -> allIssues.get(i).getId())
											.collect(Collectors.toList());
									issue.setParents(FXCollections.observableArrayList(parents));
								} else {
									issue.setParents(FXCollections.observableArrayList());
								}
								

								return true;
							});
		});
		return parentsBox;
	}
	
	private LabelDisplayBox createLabelBox(Stage stage) {
		final LabelDisplayBox labelBox = new LabelDisplayBox(issue.getLabels(), true);
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
					.setWindowTitle("Choose Labels")
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

	private Parent createAssigneeBox(Stage stage) {
		
		final ListableDisplayBox assigneeBox = new ListableDisplayBox("Assignee", issue.getAssignee());
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
					.setWindowTitle("Choose Assignee")
					.setMultipleSelection(false)
					.setInitialCheckedState(existingIndices)
					.show()
					.thenApply((response) -> {
							TurboCollaborator assignee = response.size() > 0 ? allAssignees.get(response.get(0)) : null;
							assigneeBox.setListableItem(assignee);
							issue.setAssignee(assignee);
							return true;
						});
		});
		return assigneeBox;
	}

	private Parent createMilestoneBox(Stage stage) {
		
		final ListableDisplayBox milestoneBox = new ListableDisplayBox("Milestone", issue.getMilestone());
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
					.setWindowTitle("Choose Milestone")
					.setMultipleSelection(false)
					.setInitialCheckedState(existingIndices)
					.show()
					.thenApply((response) -> {
							TurboMilestone milestone = response.size() > 0 ? allMilestones.get(response.get(0)) : null;
							milestoneBox.setListableItem(milestone);
							issue.setMilestone(milestone);
							return true;
						});
		});
		return milestoneBox;
	}
}
