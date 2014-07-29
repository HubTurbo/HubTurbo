package ui.issuepanel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import model.Model;
import model.TurboIssue;
import model.TurboLabel;
import model.TurboMilestone;
import model.TurboUser;
import ui.CheckboxListDialog;
import ui.LabelCheckboxListDialog;
import ui.LabelDisplayBox;
import ui.ListableDisplayBox;
import ui.ParentIssuesDisplayBox;
import util.Browse;

public class IssueEditDisplay extends VBox{
	private static final int ELEMENT_SPACING = 5;
	protected static String EDIT_BTN_TXT = "\uf058";
	protected static String BACK_BTN_TXT = " \uf0a4 ";
	
	private Text issueIdText;
	private TextArea editableIssueDesc;
	private WebView issueDesc;
	private ToggleButton descEditMode;
	private VBox descArea;
	
	protected static final String ISSUE_DETAILS_BTN_TXT = "Details >>";
	protected static final int LINE_HEIGHT = 18;
	protected static final int TITLE_ROW_NUM = 3;
	protected static final int DESC_ROW_NUM = 8;
	
	private final TurboIssue issue;
	private final Model model;
	private final Stage parentStage;
	private final WeakReference<IssueDisplayPane> parentContainer;
	private boolean focusRequested;
	
	private ArrayList<ChangeListener<?>> changeListeners = new ArrayList<ChangeListener<?>>();
	
	public IssueEditDisplay(TurboIssue displayedIssue, Stage parentStage, Model model, IssueDisplayPane parent, boolean focusRequested){
		this.issue = displayedIssue;
		this.model = model;
		this.parentStage = parentStage;
		this.parentContainer = new WeakReference<IssueDisplayPane>(parent);
		this.focusRequested = focusRequested;
		setup();
	}

	
	private void setup(){
		setPadding(new Insets(15));
		setSpacing(ELEMENT_SPACING);
		setupDescription();
		setVgrow(descArea, Priority.ALWAYS);
		getChildren().addAll(top(), descArea, bottom());
	}
	
	private void setupDescriptionDisplays(){
		setupIssueDescriptionDisplay();
		setupEditableDescription();
	}
	
	private void setupDescription(){
		descArea = new VBox();
		
		HBox container = new HBox();
		initialiseDescEditButton();
		container.getChildren().add(descEditMode);
		container.setAlignment(Pos.BASELINE_RIGHT);
		
		setupDescriptionDisplays();
		
		descArea.getChildren().addAll(container, issueDesc);
	}
	
	private void initialiseDescEditButton(){
		descEditMode = new ToggleButton();
		descEditMode.getStyleClass().addAll("button-github-octicon", "borderless-toggle-button");
		descEditMode.setText(EDIT_BTN_TXT);
		WeakReference<IssueEditDisplay> selfRef = new WeakReference<>(this);
		WeakReference<ToggleButton> btnRef = new WeakReference<>(descEditMode);
		descEditMode.setOnAction((ActionEvent e) -> {
			boolean editMode = btnRef.get().isSelected();
			selfRef.get().toggleDescriptionAreaForEditMode(editMode);
		});
	}
	
	private void toggleDescriptionAreaForEditMode(boolean edit){
		descArea.getChildren().remove(1);
		if(edit){
			descArea.getChildren().add(editableIssueDesc);
			descEditMode.setText(BACK_BTN_TXT);
		}else{
			descArea.getChildren().add(issueDesc);
			descEditMode.setText(EDIT_BTN_TXT);
		}
	}
	
	
	private ChangeListener<String> createIssueTitleChangeListener(){
		WeakReference<TurboIssue> issueRef = new WeakReference<TurboIssue>(issue);
		ChangeListener<String> listener = (observable, oldValue, newValue) -> {
			TurboIssue issue = issueRef.get();
			if(issue != null){
				issueRef.get().setTitle(newValue);
			}
		};
		changeListeners.add(listener);
		return listener;
	}
	
	private ChangeListener<String> createIssueDescriptionChangeListener(){
		WeakReference<TurboIssue> issueRef = new WeakReference<TurboIssue>(issue);
		WeakReference<IssueEditDisplay> selfRef = new WeakReference<IssueEditDisplay>(this);
		ChangeListener<String> listener =  (observable, oldValue, newValue) -> {
			TurboIssue issue = issueRef.get();
        	if(issue != null){
        		issue.setDescription(newValue);
        		selfRef.get().loadIssueDescriptionViewContent();
        	}
		};
		changeListeners.add(listener);
		return listener;
	}
	
	private TextArea createIssueTitle(){
		TextArea issueTitle = new TextArea(issue.getTitle());
		issueTitle.setPromptText("Title");
		issueTitle.setPrefRowCount(TITLE_ROW_NUM);
		issueTitle.setPrefColumnCount(42);
		issueTitle.setWrapText(true);
		issueTitle.textProperty().addListener(new WeakChangeListener<String>(createIssueTitleChangeListener()));
		return issueTitle;
	}
	
	protected void updateIssueId(Integer id){
		if(issueIdText != null){
			issueIdText.setText(issue.getId() == 0 ? "" : "#" + issue.getId());
		}
	}
	
	private HBox createTopTitle(){
		HBox title = new HBox();
		title.setAlignment(Pos.BASELINE_LEFT);
		title.setSpacing(ELEMENT_SPACING);
		
		// TODO ALIGNMENT
		issueIdText = new Text(issue.getId() == 0 ? "" : "#" + issue.getId());
		HBox issueId = new HBox();
		issueId.getChildren().add(issueIdText);
		issueId.setStyle("-fx-font-size: 16pt;");
		issueId.setOnMouseClicked(e -> {
			Browse.browse(issue.getHtmlUrl());
		});
		
		TextArea issueTitle = createIssueTitle();
		if (focusRequested) {
			Platform.runLater(() -> issueTitle.requestFocus());
		}
		
		Parent statusBox = createStatusBox(parentStage);
		
		VBox topLeft = new VBox();
		topLeft.setSpacing(5);
		topLeft.setAlignment(Pos.CENTER);
		topLeft.getChildren().addAll(issueId, statusBox);
		
		int maxTitleHeight = TITLE_ROW_NUM * LINE_HEIGHT;
		issueTitle.setMaxHeight(maxTitleHeight);
		title.setMaxHeight(maxTitleHeight);
		topLeft.setMaxHeight(maxTitleHeight);
		
		title.getChildren().addAll(topLeft, issueTitle);
		title.setAlignment(Pos.CENTER);
		return title;
	}
	
	private void setupEditableDescription(){
		editableIssueDesc = new TextArea(issue.getDescription());
		editableIssueDesc.setPrefColumnCount(42);
		editableIssueDesc.setWrapText(true);
		editableIssueDesc.setPromptText("Description");
		editableIssueDesc.textProperty().addListener(new WeakChangeListener<String>(createIssueDescriptionChangeListener()));
		editableIssueDesc.setPrefHeight(700); //Large number so textarea scales to fill remaining space
	}
	
	private void setupIssueDescriptionDisplay(){
		issueDesc = new WebView();
//		int issueDescHeight = DESC_ROW_NUM * LINE_HEIGHT;
//		issueDesc.setPrefHeight(issueDescHeight);
		loadIssueDescriptionViewContent();
	}
	
	private void loadIssueDescriptionViewContent(){
		if(issueDesc != null){
			issueDesc.getEngine().loadContent(issue.getDescriptionMarkup());
		}
	}
	
	private Parent top() {

		HBox title = createTopTitle();
		
		String issueCreatorName = issue.getCreator() == null ? "you" : issue.getCreator();
		String issueCreatedDate = issue.getCreatedAt() == null ? "" : " on " + issue.getCreatedAt();
		Label issueCreator = new Label("created by " + issueCreatorName + issueCreatedDate);
		issueCreator.getStyleClass().add("issue-creator");
		HBox issueCreatorContainer = new HBox();
		HBox.setHgrow(issueCreatorContainer, Priority.ALWAYS);
		issueCreatorContainer.setAlignment(Pos.BASELINE_RIGHT);
		issueCreatorContainer.getChildren().add(issueCreator);

		VBox top = new VBox();
		top.setSpacing(ELEMENT_SPACING);
		top.getChildren().addAll(title, issueCreatorContainer);
		top.setMaxHeight(title.getMaxHeight() + issueCreatorContainer.getMaxHeight());
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
		
		Separator separator = new Separator();
		separator.setPadding(new Insets(5));
		
		HBox detailsButton = createIssueDetailsButton();
		HBox buttons = createButtons(parentStage);

		VBox bottom = new VBox();
		bottom.setSpacing(ELEMENT_SPACING);
		bottom.getChildren().addAll(parents, milestone, labels, assignee, separator, buttons, detailsButton);

		return bottom;
	}
	
	private HBox createButtons(Stage stage) {
		HBox buttons = new HBox();
		HBox.setHgrow(buttons, Priority.ALWAYS);
		buttons.setAlignment(Pos.BASELINE_RIGHT);
		buttons.setSpacing(ELEMENT_SPACING);

		Button cancel = new Button("Cancel");
		HBox.setHgrow(cancel, Priority.ALWAYS);
		cancel.setMaxWidth(Double.MAX_VALUE);
		cancel.setOnMouseClicked(e -> {
			parentContainer.get().handleCancelClicked();
		});

		Button done = new Button("Done");
		HBox.setHgrow(done, Priority.ALWAYS);
		done.setMaxWidth(Double.MAX_VALUE);
		done.setOnMouseClicked(e -> {
			parentContainer.get().handleDoneClicked();
		});

		buttons.getChildren().addAll(done, cancel);
		return buttons;
	}

	private HBox createIssueDetailsButton(){
		HBox container = new HBox();
		HBox.setHgrow(container, Priority.ALWAYS);
		
		ToggleButton details = new ToggleButton(ISSUE_DETAILS_BTN_TXT);
		HBox.setHgrow(details, Priority.ALWAYS);
		details.setMaxWidth(Double.MAX_VALUE);
		WeakReference<ToggleButton> ref = new WeakReference<ToggleButton>(details);
		details.setOnAction((ActionEvent e) -> {
		    boolean selected = ref.get().selectedProperty().get();
		    parentContainer.get().showIssueDetailsDisplay(selected);
		});
		
		details.setSelected(parentContainer.get().expandedIssueView);
		container.getChildren().add(details);
		
		return container;
	}
	
	private Parent createParentsBox(Stage stage) {
		final ParentIssuesDisplayBox parentsBox = new ParentIssuesDisplayBox(issue);
		List<TurboIssue> allIssues = model.getIssues();
		
		parentsBox.setOnMouseClicked((e) -> {
			Integer originalParent = issue.getParentIssue();
			Integer indexForExistingParent = model.getIndexOfIssue(originalParent);
			ArrayList<Integer> existingIndices = new ArrayList<Integer>();
			if(indexForExistingParent > 0){
				existingIndices.add(indexForExistingParent);
			}
			
			(new CheckboxListDialog(stage, FXCollections
					.observableArrayList(allIssues)))
					.setWindowTitle("Choose Parents")
					.setMultipleSelection(false)
					.setInitialCheckedState(existingIndices)
					.show()
					.thenApply((List<Integer> response) -> {
						
						boolean wasAnythingSelected = response.size() > 0;
						if (wasAnythingSelected) {
							Integer parent = response.size() > 0 ? allIssues.get(response.get(0)).getId() : null;
							issue.setParentIssue(parent);
							//TODO:
//							model.processInheritedLabels(issue, originalParent);
						} else {
							issue.setParentIssue(-1);
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
				if(existingIndex != -1){
					existingIndices.add(existingIndex);
				}
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
				if(existingIndex != -1){
					existingIndices.add(existingIndex);
				}
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
