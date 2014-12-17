package ui.issuepanel.expanded;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ui.CheckboxListDialog;
import ui.EditableMarkupPopup;
import ui.LabelCheckboxListDialog;
import ui.LabelDisplayBox;
import ui.ListableDisplayBox;
import ui.ParentIssuesDisplayBox;
import ui.TraversableTextArea;
import ui.UI;
import util.Browse;

public class IssueEditDisplay extends VBox{
	private static final Logger logger = LogManager.getLogger(IssueEditDisplay.class.getName());
	private static final int ELEMENT_SPACING = 5;
	protected static final String DEFAULT_WEB_CSS = 
			"<style type=\"text/css\">"
			+ "img{"
			+ "max-width: 100%;"
			+ "}"
			+ "body {"
			+ "font-family: System;"
			+ "font-size: 13px"
			+ "}"
			+ "</style>";
	protected static String EDIT_BTN_TXT = "\uf058";
	protected static String BACK_BTN_TXT = " \uf0a4 ";
	protected static String POPUP_BTN_TXT = "\uf07f";
	protected static final String ISSUE_DETAILS_BTN_TXT = "Details >>";
	protected static final int LINE_HEIGHT = 18;
	protected static final int TITLE_ROW_NUM = 3;
	protected static final int DESC_ROW_NUM = 8;
	protected static final KeyCombination SAVE_ISSUE_SHORTCUT = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);
	
	private Text issueIdText;
	private TraversableTextArea editableIssueDesc;
	private WebView issueDesc;
	private ToggleButton descEditMode;
	private Button descPopup;
	private VBox descArea;
	
	private LabelDisplayBox statusBox;
	private Parent parents;
	private Parent milestone;
	private Parent labels;
	private Parent assignee;
	
	private Button cancel;
	private Button save;
	
	private final TurboIssue issue;
	private final Model model;
	private final Stage parentStage;
	private final UI ui;
	private final WeakReference<IssueDisplayPane> parentContainer;
	private boolean focusRequested;
	private IssueCommentsDisplay issueCommentsDisplay;
	
	private ArrayList<ChangeListener<?>> changeListeners = new ArrayList<ChangeListener<?>>();
	
	public IssueEditDisplay(UI ui, TurboIssue displayedIssue, Stage parentStage, Model model, IssueDisplayPane parent, boolean focusRequested){
		this.ui = ui;
		this.issue = displayedIssue;
		this.model = model;
		this.parentStage = parentStage;
		this.parentContainer = new WeakReference<IssueDisplayPane>(parent);
		this.focusRequested = focusRequested;
		this.issueCommentsDisplay = new IssueCommentsDisplay(ui, issue);
		setup();
		setupKeyboardShortcuts();
	}

	
	private void setup(){
		setPadding(new Insets(15));
		setSpacing(ELEMENT_SPACING);
		setupDescription();
		setVgrow(descArea, Priority.ALWAYS);
		getChildren().addAll(top(), descArea, bottom());
	}
	
	private void triggerClick(Node node){
		Event.fireEvent(node, 
				new MouseEvent(MouseEvent.MOUSE_CLICKED, 
						0, 0, 0, 0, MouseButton.PRIMARY, 1, 
						true, true, true, true, true, true, true, true, true, true, null));
	}
	
	private void handleEscKeyPressed(){
		if(this.isFocused()){
			cancel.fire();
		}else{
			requestFocus();
		}
	}
	private void setupKeyboardShortcuts(){
		WeakReference<IssueEditDisplay> selfRef = new WeakReference<>(this);
		this.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
			if(SAVE_ISSUE_SHORTCUT.match(e)){
				parentContainer.get().handleSaveClicked();
			}else if(!e.isShiftDown()){
				KeyCode code = e.getCode();
				switch(code){
				case ESCAPE:
					selfRef.get().handleEscKeyPressed();
					break;
				case D:
					descEditMode.fire();
					break;
				case P:
					selfRef.get().triggerClick(parents);
					break;
				case S:
					selfRef.get().triggerClick(statusBox);
					break;
				case L:
					selfRef.get().triggerClick(labels);
					break;
				case A:
					selfRef.get().triggerClick(assignee);
					break;
				case M:
					selfRef.get().triggerClick(milestone);
					break;
				default:
					break;
				}
			}
		});
	}
	
	private boolean descriptionIsEmpty(){
		return issue.getDescription().isEmpty();
	}
	
	private void setupDescriptionDisplays(){
		setupIssueDescriptionDisplay();
		setupEditableDescription();
	}
	
	private void setupDescription(){
		descArea = new VBox();
		
		HBox container = new HBox();
		initialiseDescEditButton();
		initialDescPopupButton();
		
		container.getChildren().addAll(descPopup, descEditMode);
		container.setAlignment(Pos.BASELINE_RIGHT);
		
		setupDescriptionDisplays();
		
		descArea.getChildren().add(container);
		setDescriptionAreaContentForEditMode(descEditMode.isSelected());
	}
	
	private void initialiseDescEditButton(){
		descEditMode = new ToggleButton();
		descEditMode.setFocusTraversable(false);
		descEditMode.getStyleClass().addAll("button-github-octicon", "borderless-toggle-button");
		boolean isEditMode = descriptionIsEmpty();
		setDescModeButtonText(isEditMode);
		descEditMode.setSelected(isEditMode);
		
		WeakReference<IssueEditDisplay> selfRef = new WeakReference<>(this);
		WeakReference<ToggleButton> btnRef = new WeakReference<>(descEditMode);
		descEditMode.setOnAction((ActionEvent e) -> {
			boolean editMode = btnRef.get().isSelected();
			selfRef.get().toggleDescriptionAreaForEditMode(editMode);
		});
	}
	
	private void initialDescPopupButton(){
		descPopup = new Button();
		descPopup.setFocusTraversable(false);
		descPopup.setText(POPUP_BTN_TXT);
		descPopup.getStyleClass().addAll("button-github-octicon", "borderless-button");
		descPopup.setOnMouseClicked(e -> {
			EditableMarkupPopup popup = createDescPopup();
			popup.show();
		});
	}
	
	private EditableMarkupPopup createDescPopup(){
		EditableMarkupPopup popup = new EditableMarkupPopup("Done");
		popup.setDisplayedText(issue.getDescriptionMarkup(), issue.getDescription());
		
		WeakReference<EditableMarkupPopup> ref = new WeakReference<>(popup);
		popup.setEditModeCompletion(() -> {
			editableIssueDesc.setText(ref.get().getText());
		});
		return popup;
	}
	
	private void setDescModeButtonText(boolean editMode){
		if(editMode){
			descEditMode.setText(BACK_BTN_TXT);
		}else{
			descEditMode.setText(EDIT_BTN_TXT);
		}
	}
	
	private void setDescriptionAreaContentForEditMode(boolean edit){
		if(edit){
			descArea.getChildren().add(editableIssueDesc);
			editableIssueDesc.requestFocus();
		}else{
			descArea.getChildren().add(issueDesc);
		}
	}
	
	private void toggleDescriptionAreaForEditMode(boolean edit){
		descArea.getChildren().remove(1);
		setDescModeButtonText(edit);
		setDescriptionAreaContentForEditMode(edit);
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
	
	private TraversableTextArea createIssueTitle(){
		TraversableTextArea issueTitle = new TraversableTextArea(issue.getTitle());
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
		
		TraversableTextArea issueTitle = createIssueTitle();
		if (focusRequested) {
			Platform.runLater(() -> issueTitle.requestFocus());
		}
		
		statusBox = createStatusBox(parentStage);
		
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
		editableIssueDesc = new TraversableTextArea(issue.getDescription());
		editableIssueDesc.setPrefColumnCount(42);
		editableIssueDesc.setWrapText(true);
		editableIssueDesc.setPromptText("Description");
		editableIssueDesc.textProperty().addListener(new WeakChangeListener<String>(createIssueDescriptionChangeListener()));
		editableIssueDesc.setPrefHeight(700); //Large number so textarea scales to fill remaining space
	}
	
	private void setupIssueDescriptionDisplay(){
		issueDesc = new WebView();
		loadIssueDescriptionViewContent();
	}
	
	private void loadIssueDescriptionViewContent(){
		if(issueDesc != null){
			String content = DEFAULT_WEB_CSS + issue.getDescriptionMarkup();
			issueDesc.getEngine().loadContent(content);
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
						logger.error(ex.getLocalizedMessage(), ex);
						return false;
					});
		});
		
		statusBox.setMaxWidth(0);
		return statusBox;
	}

	private Parent bottom() {

		parents = createParentsBox(parentStage);
		milestone = createMilestoneBox(parentStage);
		labels = createLabelBox(parentStage);
		assignee = createAssigneeBox(parentStage);
		
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

		cancel = new Button("Cancel");
		HBox.setHgrow(cancel, Priority.ALWAYS);
		cancel.setMaxWidth(Double.MAX_VALUE);
		cancel.setOnAction(e -> {
			parentContainer.get().handleCancelClicked();
		});

		save = new Button("Save");
		HBox.setHgrow(save, Priority.ALWAYS);
		save.setMaxWidth(Double.MAX_VALUE);
		save.setOnAction(e -> {
			parentContainer.get().handleSaveClicked();
		});

		buttons.getChildren().addAll(save, cancel);
		return buttons;
	}

	private HBox createIssueDetailsButton(){
		HBox container = new HBox();
		HBox.setHgrow(container, Priority.ALWAYS);
		
		ToggleButton details = new ToggleButton(ISSUE_DETAILS_BTN_TXT);
		HBox.setHgrow(details, Priority.ALWAYS);
		details.setMaxWidth(Double.MAX_VALUE);
		details.setOnAction(e -> {
			issueCommentsDisplay.toggle();
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
						} else {
							issue.setParentIssue(-1);
						}
						return true;
					})
					.exceptionally(ex -> {
						logger.error(ex.getLocalizedMessage(), ex);
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
					logger.error(ex.getLocalizedMessage(), ex);
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
						logger.error(ex.getLocalizedMessage(), ex);
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
						logger.error(ex.getLocalizedMessage(), ex);
						return false;
					});
		});
		return milestoneBox;
	}

}
