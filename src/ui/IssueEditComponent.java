package ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Model;
import model.TurboIssue;
import model.TurboLabel;
import model.TurboMilestone;
import model.TurboUser;

public class IssueEditComponent extends VBox {

	private final TurboIssue issue;
	private final Model model;
	private final Stage parentStage;
	
	private final CompletableFuture<String> response;
	
	public IssueEditComponent(TurboIssue displayedIssue, Stage parentStage, Model model) {
		this.issue = displayedIssue;
		this.model = model;
		this.parentStage = parentStage;
		this.response = new CompletableFuture<>();
		setup();
	}
	
	public CompletableFuture<String> getResponse() {
		return response;
	}

	private static final int TITLE_SPACING = 5;
	private static final int ELEMENT_SPACING = 10;
	private static final int MIDDLE_SPACING = 20;

	private ArrayList<ChangeListener<?>> changeListeners = new ArrayList<ChangeListener<?>>();

	private void setup() {
		setPadding(new Insets(15));
		setSpacing(MIDDLE_SPACING);
		getChildren().addAll(top(), bottom());
	}
	
	private ChangeListener<String> createIssueTitleChangeListener(){
		WeakReference<TurboIssue> issueRef = new WeakReference<TurboIssue>(issue);
		ChangeListener<String> listener = (observable, oldValue, newValue) -> {
			TurboIssue issue = issueRef.get();
			if(issue != null){
				issueRef.get().setTitle((String)newValue);
			}
		};
		changeListeners.add(listener);
		return listener;
	}
	
	private ChangeListener<String> createIssueDescriptionChangeListener(){
		WeakReference<TurboIssue> issueRef = new WeakReference<TurboIssue>(issue);
		ChangeListener<String> listener =  (observable, oldValue, newValue) -> {
			TurboIssue issue = issueRef.get();
        	if(issue != null){
        		issue.setDescription((String)newValue);
        	}
		};
		changeListeners.add(listener);
		return listener;
	}
	
	private Parent top() {

		HBox title = new HBox();
		title.setAlignment(Pos.BASELINE_LEFT);
		title.setSpacing(TITLE_SPACING);

		Label issueId = new Label("#" + issue.getId());
		TextField issueTitle = new TextField(issue.getTitle());
		issueTitle.setPromptText("Title");
		issueTitle.textProperty().addListener(
				new WeakChangeListener<String>(createIssueTitleChangeListener()));
		
		Parent statusBox = createStatusBox(parentStage);
		title.getChildren().addAll(issueId, issueTitle, statusBox);
		
		TextArea issueDesc = new TextArea(issue.getDescription());
		issueDesc.setPrefRowCount(8);
		issueDesc.setPrefColumnCount(42);
		issueDesc.setWrapText(true);
		issueDesc.setPromptText("Description");
		issueDesc.textProperty().addListener(new WeakChangeListener<String>(createIssueDescriptionChangeListener()));

		VBox top = new VBox();
		top.setSpacing(ELEMENT_SPACING);
		top.getChildren().addAll(title, issueDesc);

		return top;
	}
	
	private LabelDisplayBox createStatusBox(Stage stage) {
		ObservableList<TurboLabel> statusLabel = FXCollections.observableArrayList();
		for (TurboLabel label : issue.getLabels()) {
			if (label.getGroup() != null && label.getGroup().equals("status")) {
				statusLabel.add(label);
				break;
			}
		}
		final LabelDisplayBox statusBox = new LabelDisplayBox(statusLabel, true, "Status");
		ObservableList<TurboLabel> allStatuses = FXCollections.observableArrayList();
		for (TurboLabel label : model.getLabels()) {
			if (label.getGroup() != null && label.getGroup().equals("status")) {
				allStatuses.add(label);
			}
		}
		
		statusBox.setOnMouseClicked((e) -> {
			(new LabelCheckboxListDialog(stage, allStatuses))
				.setInitialChecked(issue.getLabels())
				.show().thenApply(
					(List<TurboLabel> response) -> {
						ObservableList<TurboLabel> issueLabels = issue.getLabels();
						issueLabels.removeIf(label -> label.getGroup() != null && label.getGroup().equals("status"));
						issueLabels.addAll(FXCollections.observableArrayList(response));
						issue.setLabels(issueLabels);
						statusLabel.setAll(FXCollections.observableArrayList(response));
						return true;
					}).exceptionally(ex -> {
						ex.printStackTrace();
						return false;
					});
		});
		
		statusBox.setMaxWidth(0);
		return statusBox;
	}

	private Parent bottom() {

		Parent parents = createParentsBox(parentStage);
		Parent milestone = createMilestoneBox(parentStage);
		Parent labels = createLabelBox(parentStage);
		Parent assignee = createAssigneeBox(parentStage);

		HBox buttons = createButtons(parentStage);

		VBox bottom = new VBox();
		bottom.setSpacing(ELEMENT_SPACING);
		bottom.getChildren().addAll(parents, milestone, labels, assignee, buttons);

		return bottom;
	}

	private HBox createButtons(Stage stage) {
		HBox buttons = new HBox();
		buttons.setAlignment(Pos.BASELINE_RIGHT);
		buttons.setSpacing(8);

		Button cancel = new Button();
		cancel.setText("Cancel");
		cancel.setOnMouseClicked(e -> {
			response.complete("cancel");
		});

		Button ok = new Button();
		ok.setText("OK");
		ok.setOnMouseClicked(e -> {
			response.complete("ok");
		});

		buttons.getChildren().addAll(ok, cancel);
		return buttons;
	}


	private Parent createParentsBox(Stage stage) {
		final ParentIssuesDisplayBox parentsBox = new ParentIssuesDisplayBox(issue.getParentsReference(), true);
		List<TurboIssue> allIssues = model.getIssues();
		
		parentsBox.setOnMouseClicked((e) -> {
			List<Integer> originalParents = issue.getParents();
			List<Integer> indicesForExistingParents = originalParents.stream()
					.map((parent) -> {
						for (int i = 0; i < allIssues.size(); i++) {
							if (allIssues.get(i).getId() == parent) {
								return i;
							}
						}
						assert false;
						return -1;
					}).collect(Collectors.toList());

			(new CheckboxListDialog(stage, FXCollections
					.observableArrayList(allIssues)))
					.setWindowTitle("Choose Parents")
					.setMultipleSelection(true)
					.setInitialCheckedState(indicesForExistingParents)
					.show()
					.thenApply((List<Integer> response) -> {
						
						boolean wasAnythingSelected = response.size() > 0;
						if (wasAnythingSelected) {
							List<Integer> parents = response.stream()
									.map((i) -> allIssues.get(i).getId())
									.collect(Collectors.toList());
							issue.setParents(FXCollections.observableArrayList(parents));
							model.processInheritedLabels(issue, originalParents);
						} else {
							issue.setParents(FXCollections.observableArrayList());
						}
						return true;
					})
					.exceptionally(ex -> {
						ex.printStackTrace();
						return false;
					});
		});
		return parentsBox;
	}

	private LabelDisplayBox createLabelBox(Stage stage) {
		ObservableList<TurboLabel> nonStatusLabels = FXCollections.observableArrayList();
		for (TurboLabel label : issue.getLabels()) {
			if (label.getGroup() == null || !label.getGroup().equals("status")) {
				nonStatusLabels.add(label);
			}
		}
		
		final LabelDisplayBox labelBox = new LabelDisplayBox(nonStatusLabels, true, "Labels");
		ObservableList<TurboLabel> allLabels = FXCollections.observableArrayList();
		for (TurboLabel label : model.getLabels()) {
			if (label.getGroup() == null || !label.getGroup().equals("status")) {
				allLabels.add(label);
			}
		}
		
		labelBox.setOnMouseClicked((e) -> {
			(new LabelCheckboxListDialog(stage, allLabels))
				.setInitialChecked(issue.getLabels())
				.show().thenApply(
					(List<TurboLabel> response) -> {
						ObservableList<TurboLabel> issueLabels = issue.getLabels();
						issueLabels.removeIf(label -> label.getGroup() == null || !label.getGroup().equals("status"));
						issueLabels.addAll(FXCollections.observableArrayList(response));
						issue.setLabels(issueLabels);
						nonStatusLabels.setAll(FXCollections.observableArrayList(response));
						return true;
					})
				.exceptionally(ex -> {
					ex.printStackTrace();
					return false;
				});
		});
		return labelBox;
	}

	private Parent createAssigneeBox(Stage stage) {
		
		final ListableDisplayBox assigneeBox = new ListableDisplayBox("Assignee", issue.getAssignee());
		List<TurboUser> allAssignees = model.getCollaborators();
		
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
			
			(new CheckboxListDialog(stage, FXCollections
					.observableArrayList(allAssignees)))
					.setWindowTitle("Choose Assignee")
					.setMultipleSelection(false)
					.setInitialCheckedState(existingIndices)
					.show()
					.thenApply((response) -> {
							TurboUser assignee = response.size() > 0 ? allAssignees.get(response.get(0)) : null;
							assigneeBox.setListableItem(assignee);
							issue.setAssignee(assignee);
							return true;
						})
					.exceptionally(ex -> {
						ex.printStackTrace();
						return false;
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
			
			(new CheckboxListDialog(stage, FXCollections
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
						})
					.exceptionally(ex -> {
						ex.printStackTrace();
						return false;
					});
		});
		return milestoneBox;
	}
}
