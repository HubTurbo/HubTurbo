package ui.components.pickers;

import backend.resource.TurboIssue;
import backend.resource.TurboMilestone;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * This class handles the display of milestone selection status and the user's inputs
 */
public class MilestonePickerDialog extends Dialog<MilestonePickerDialogResponse> {
    private static final String OCTICON_ARROW = "\uf03e";
    private static final String DIALOG_TITLE = "Select Milestone";
    private static final int DETAILED_MILESTONE_HEIGHT = 30;
    private static final int PREV_ASSIGNED_MILESTONE_HEIGHT = 30;

    private final List<PickerMilestone> originalMilestones = new ArrayList<>();
    private FlowPane assignedMilestoneBox;
    private VBox matchingMilestonesBox;
    private TextField inputField;
    private MilestonePickerState state;

    /**
     * The issue and the originalMilestones list provided should come from the same repository
     */
    public MilestonePickerDialog(Stage stage, TurboIssue issue, List<TurboMilestone> milestones) {
        assert areFromSameRepo(issue, milestones);
        initOwner(stage);
        setTitle(DIALOG_TITLE);
        setupButtons(getDialogPane());
        setConfirmResultConverter();
        originalMilestones.addAll(convertToPickerMilestones(issue, milestones));
        state = new MilestonePickerState(originalMilestones);
        initUI();
        setupKeyEvents();
        setInputFieldToDefaultMilestone();
    }

    /**
     * Fills the input field of milestone picker dialog with default milestone's title
     */
    private void setInputFieldToDefaultMilestone() {
        Optional<PickerMilestone> defaultMilestone = PickerMilestone.getDefaultMilestone(originalMilestones);
        defaultMilestone.map(PickerMilestone::getTitle).ifPresent(this::fillInputFieldWithMilestoneName);
    }

    private boolean areFromSameRepo(TurboIssue issue, List<TurboMilestone> milestones) {
        if (milestones.isEmpty()) return true;
        return issue.getRepoId().equals(milestones.get(0).getRepoId());
    }

    private void fillInputFieldWithMilestoneName(String milestoneName) {
        inputField.setText(milestoneName);
    }

    private void setupKeyEvents() {
        inputField.textProperty().addListener((observable, oldValue, newValue) -> {
            handleUpdatedInput(newValue);
        });
    }

    private void handleUpdatedInput(String userInput) {
        state = new MilestonePickerState(originalMilestones, userInput);
        refreshUI(state);
    }

    private List<PickerMilestone> convertToPickerMilestones(TurboIssue issue, List<TurboMilestone> milestones) {
        List<PickerMilestone> originalMilestones = new ArrayList<>();

        milestones.stream()
                .forEach(milestone -> {
                    PickerMilestone convertedMilestone = new PickerMilestone(milestone);
                    if (isExistingMilestone(issue, convertedMilestone)) {
                        convertedMilestone.setExisting(true);
                    }
                    originalMilestones.add(convertedMilestone);
                });

        Collections.sort(originalMilestones);

        return originalMilestones;
    }

    private boolean isExistingMilestone(TurboIssue issue, PickerMilestone milestone) {
        if (!issue.getMilestone().isPresent()) return false;
        return issue.getMilestone().get() == milestone.getId();
    }

    private void setupButtons(DialogPane milestonePickerDialogPane) {
        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        milestonePickerDialogPane.getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
    }

    private void setConfirmResultConverter() {
        setResultConverter((dialogButton) -> {
            List<PickerMilestone> finalList = state.getAllMilestones();
            Optional<PickerMilestone> selectedMilestone = PickerMilestone.getSelectedMilestone(finalList);
            return new MilestonePickerDialogResponse(dialogButton, selectedMilestone.map(PickerMilestone::getId));
        });
    }

    private Node setMouseClickForNode(Node node, String milestoneName) {
        node.setOnMouseClicked(e -> handleMouseClick(milestoneName));
        return node;
    }

    private void handleMouseClick(String milestoneName) {
        inputField.setDisable(true);
        state.toggleExactMatchMilestone(milestoneName);
        refreshUI(state);
    }

    private void initUI() {
        VBox milestoneDialogBox = createMilestoneDialogBox();
        assignedMilestoneBox = createAssignedMilestoneBox();
        matchingMilestonesBox = createMatchingMilestonesBox();
        inputField = createInputField();

        milestoneDialogBox.getChildren().addAll(assignedMilestoneBox, inputField, matchingMilestonesBox);

        getDialogPane().setContent(milestoneDialogBox);
        refreshUI(state);
        Platform.runLater(inputField::requestFocus);
    }

    private void refreshUI(MilestonePickerState state) {
        populateAssignedMilestone(state.getAllMilestones(), assignedMilestoneBox);
        populateMatchingMilestones(state.getBestMatchingMilestones(), matchingMilestonesBox);
    }

    private void populateAssignedMilestone(List<PickerMilestone> pickerMilestoneList, FlowPane assignedMilestonePane) {
        assignedMilestonePane.getChildren().clear();
        updateExistingMilestone(PickerMilestone.getExistingMilestone(pickerMilestoneList), assignedMilestonePane);
        addAssignmentIndicator(assignedMilestonePane);
        updateNewlyAssignedMilestone(PickerMilestone.getSelectedMilestone(pickerMilestoneList), assignedMilestonePane);
    }

    private void populateMatchingMilestones(List<PickerMilestone> matchingMilestoneList, VBox matchingMilestones) {
        matchingMilestones.getChildren().clear();
        matchingMilestoneList.stream()
                .sorted()
                .forEach(milestone -> matchingMilestones.getChildren().add(
                        setMouseClickForNode(milestone.getDetailedMilestoneNode(), milestone.getTitle())));
    }

    private void addAssignmentIndicator(FlowPane assignedMilestoneStatus) {
        Label assignmentIndicator = new Label(OCTICON_ARROW);
        assignmentIndicator.getStyleClass().add("octicon");
        assignmentIndicator.setPadding(new Insets(5, 5, 5, 5));
        assignedMilestoneStatus.getChildren().add(assignmentIndicator);
    }

    private void updateNewlyAssignedMilestone(Optional<PickerMilestone> selectedMilestone,
                                              FlowPane assignedMilestoneStatus) {
        HBox newlyAssignedMilestoneBox = createNewlyAssignedMilestoneBox();
        assignedMilestoneStatus.getChildren().add(newlyAssignedMilestoneBox);

        if (!selectedMilestone.isPresent()) return;

        Node nodeToAdd = selectedMilestone.get().getNewlyAssignedMilestoneNode();
        newlyAssignedMilestoneBox.getChildren().add(nodeToAdd);
    }

    private void updateExistingMilestone(Optional<PickerMilestone> existingMilestone, FlowPane assignedMilestonePane) {
        HBox assignedMilestoneBox = createPreviouslyAssignedMilestoneBox();
        assignedMilestonePane.getChildren().add(assignedMilestoneBox);

        if (!existingMilestone.isPresent()) return;

        Node existingMilestoneNode = setMouseClickForNode(existingMilestone.get().getExistingMilestoneNode(),
                                                          existingMilestone.get().getTitle());
        assignedMilestoneBox.getChildren().add(existingMilestoneNode);
    }

    private VBox createMilestoneDialogBox() {
        VBox dialogBox = new VBox();
        dialogBox.setSpacing(3);
        return dialogBox;
    }

    private TextField createInputField() {
        TextField inputTextField = new TextField();
        inputTextField.setId("milestonePickerTextField");
        return inputTextField;
    }

    private FlowPane createAssignedMilestoneBox() {
        FlowPane assignedMilestoneBox = new FlowPane();
        assignedMilestoneBox.setPadding(new Insets(5, 5, 5, 5));
        assignedMilestoneBox.setStyle("-fx-alignment:center;");
        return assignedMilestoneBox;
    }

    private VBox createMatchingMilestonesBox() {
        VBox milestoneGroup = new VBox();
        milestoneGroup.setPrefHeight(DETAILED_MILESTONE_HEIGHT * MilestonePickerState.BEST_MATCHING_LIMIT);
        milestoneGroup.setStyle("-fx-border-radius: 3;-fx-background-color: white;-fx-border-color: black;");
        return milestoneGroup;
    }

    private HBox createNewlyAssignedMilestoneBox() {
        HBox milestoneBox = new HBox();
        milestoneBox.setPrefWidth(140);
        milestoneBox.setPrefHeight(40);
        milestoneBox.setStyle("-fx-border-radius: 3;-fx-border-style: dotted;-fx-alignment:center");
        return milestoneBox;
    }

    private HBox createPreviouslyAssignedMilestoneBox() {
        HBox milestoneBox = new HBox();
        milestoneBox.setPrefWidth(120);
        milestoneBox.setPrefHeight(PREV_ASSIGNED_MILESTONE_HEIGHT);
        milestoneBox.setMaxHeight(PREV_ASSIGNED_MILESTONE_HEIGHT);
        milestoneBox.setStyle("-fx-border-radius: 3;-fx-border-style: dotted;-fx-alignment:center");
        return milestoneBox;
    }

}
