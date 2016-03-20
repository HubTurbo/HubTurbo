package ui.components.pickers;

import backend.resource.TurboIssue;
import backend.resource.TurboMilestone;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MilestonePickerDialog extends Dialog<Pair<ButtonType, Optional<Integer>>> {
    private static final String OCTICON_ARROW = "\uf03e";
    private static final String DIALOG_TITLE = "Select Milestone";

    private final List<PickerMilestone> originalMilestones = new ArrayList<>();
    private FlowPane assignedMilestonePane;
    private VBox matchingMilestonesPane;
    private TextField inputField;
    private MilestonePickerState state;

    /**
     * The issue and the originalMilestones list provided should come from the same repository
     */
    public MilestonePickerDialog(Stage stage, TurboIssue issue, List<TurboMilestone> milestones) {
        initOwner(stage);
        setTitle(DIALOG_TITLE);
        setupButtons(getDialogPane());
        setConfirmResultConverter();
        originalMilestones.addAll(convertToPickerMilestones(issue, milestones));
        state = new MilestonePickerState(originalMilestones);
        initUI();
        setupKeyEvents();
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
        selectExistingMilestone(originalMilestones, issue);

        return originalMilestones;
    }

    private boolean isExistingMilestone(TurboIssue issue, PickerMilestone milestone) {
        if (!issue.getMilestone().isPresent()) return false;
        return issue.getMilestone().get() == milestone.getId();
    }

    private void selectExistingMilestone(List<PickerMilestone> milestones, TurboIssue issue) {
        milestones.stream()
                .filter(milestone -> isExistingMilestone(issue, milestone))
                .forEach(milestone -> milestone.setSelected(true));
    }

    private void setupButtons(DialogPane milestonePickerDialogPane) {
        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        milestonePickerDialogPane.getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
    }

    private void setConfirmResultConverter() {
        setResultConverter((dialogButton) -> {
            List<PickerMilestone> finalList = state.getCurrentMilestonesList();
            if (hasSelectedMilestone(finalList)) {
                return new Pair<>(dialogButton, Optional.of(getSelectedMilestone(finalList).get().getId()));
            }
            return new Pair<>(dialogButton, Optional.empty());
        });
    }

    private Node setMouseClickForNode(Node node, String milestoneName) {
        node.setOnMouseClicked(e -> handleMouseClick(milestoneName));
        return node;
    }

    private void handleMouseClick(String milestoneName) {
        // We need to save this state before clearing the input field as clearing the input field
        // will change the state
        MilestonePickerState curState = state;
        inputField.clear();
        inputField.setDisable(true);
        curState.toggleExactMatchMilestone(milestoneName);
        state = curState;
        refreshUI(state);
    }

    private void initUI() {
        VBox milestoneDialogBox = createMilestoneDialogBox();
        assignedMilestonePane = createAssignedMilestoneBox();
        matchingMilestonesPane = createMatchingMilestonesBox();
        inputField = createInputField();

        milestoneDialogBox.getChildren().addAll(assignedMilestonePane, inputField, matchingMilestonesPane);

        getDialogPane().setContent(milestoneDialogBox);
        refreshUI(state);
        Platform.runLater(inputField::requestFocus);
    }

    private void refreshUI(MilestonePickerState state) {
        populateAssignedMilestone(state.getCurrentMilestonesList(), assignedMilestonePane);
        populateMatchingMilestones(state.getMatchingMilestonesList(), matchingMilestonesPane);
    }

    private void populateAssignedMilestone(List<PickerMilestone> pickerMilestoneList, FlowPane assignedMilestonePane) {
        assignedMilestonePane.getChildren().clear();
        updateExistingMilestone(getExistingMilestone(pickerMilestoneList), assignedMilestonePane);
        addAssignmentIndicator(assignedMilestonePane);
        updateNewlyAssignedMilestone(getHighlightedMilestone(pickerMilestoneList),
                getSelectedMilestone(pickerMilestoneList), assignedMilestonePane);
    }

    private void populateMatchingMilestones(List<PickerMilestone> matchingMilestoneList, VBox matchingMilestones) {
        matchingMilestones.getChildren().clear();
        matchingMilestoneList.stream()
                .sorted()
                .limit(5)
                .forEach(milestone -> matchingMilestones.getChildren().add(createDetailedMilestoneBox(milestone)));

        if (matchingMilestoneList.size() <= 5) return;

        Label hiddenMilestonesIndicator = createHiddenMilestonesIndicator(matchingMilestoneList.size() - 5);
        matchingMilestones.getChildren().add(hiddenMilestonesIndicator);
    }

    private void addAssignmentIndicator(FlowPane assignedMilestoneStatus) {
        Label assignmentIndicator = new Label(OCTICON_ARROW);
        assignmentIndicator.getStyleClass().add("octicon");
        assignmentIndicator.setPadding(new Insets(5, 5, 5, 5));
        assignedMilestoneStatus.getChildren().add(assignmentIndicator);
    }

    private void updateNewlyAssignedMilestone(Optional<PickerMilestone> highlightedMilestone,
                                              Optional<PickerMilestone> selectedMilestone,
                                              FlowPane assignedMilestoneStatus) {
        HBox newlyAssignedMilestoneBox = createNewlyAssignedMilestoneBox();
        assignedMilestoneStatus.getChildren().add(newlyAssignedMilestoneBox);

        if (!highlightedMilestone.isPresent() && !selectedMilestone.isPresent()) return;

        Node nodeToAdd = highlightedMilestone.isPresent()
                ? highlightedMilestone.get().getNewlyAssignedMilestoneNode(true)
                : selectedMilestone.get().getNewlyAssignedMilestoneNode(false);
        newlyAssignedMilestoneBox.getChildren().add(nodeToAdd);
    }

    private void updateExistingMilestone(Optional<PickerMilestone> existingMilestone, FlowPane assignedMilestonePane) {
        HBox assignedMilestoneBox = createPreviouslyAssignedMilestoneBox();
        assignedMilestonePane.getChildren().add(assignedMilestoneBox);

        if (!existingMilestone.isPresent()) return;

        Node existingMilestoneNode = setMouseClickForNode(existingMilestone.get().getSimpleNode(),
                existingMilestone.get().getTitle());
        assignedMilestoneBox.getChildren().add(existingMilestoneNode);
    }

    private HBox createDetailedMilestoneBox(PickerMilestone milestone) {
        HBox detailedMilestoneBox = new HBox();
        detailedMilestoneBox.setSpacing(3);
        detailedMilestoneBox.setPadding(new Insets(3, 3, 3, 3));
        detailedMilestoneBox.setStyle("-fx-border-width: 0 0 1 0; -fx-border-style: solid;");

        HBox milestoneNodeBox = createMilestoneNodeBox(milestone);
        Label separator = new Label("|");
        HBox milestoneDetailsBox = createMilestoneDetailsBox(milestone);

        detailedMilestoneBox.getChildren().setAll(milestoneNodeBox, separator, milestoneDetailsBox);

        return detailedMilestoneBox;
    }

    private HBox createMilestoneDetailsBox(PickerMilestone milestone) {
        HBox milestoneDetailsBox = new HBox();
        milestoneDetailsBox.setSpacing(3);
        milestoneDetailsBox.setPrefWidth(250);
        milestoneDetailsBox.setAlignment(Pos.CENTER_RIGHT);

        if (milestone.getDueDate().isPresent()) {
            Label dueDate = new Label("Due on: " + milestone.getDueDate().get().toString());
            dueDate.setPrefWidth(150);
            milestoneDetailsBox.getChildren().add(dueDate);
        }
        int totalIssues = milestone.getOpenIssues() + milestone.getClosedIssues();
        double progressValue = totalIssues > 0
                ? (double) milestone.getClosedIssues() / (milestone.getOpenIssues() + milestone.getClosedIssues()) : 0;
        MilestoneProgressBar progressBar = new MilestoneProgressBar(progressValue);
        Label progressLabel = new Label(String.format("%3.0f%%", progressValue * 100));
        progressLabel.setPrefWidth(50);
        milestoneDetailsBox.getChildren().addAll(progressBar, progressLabel);
        return milestoneDetailsBox;
    }

    private HBox createMilestoneNodeBox(PickerMilestone milestone) {
        HBox milestoneNodeBox = new HBox();
        milestoneNodeBox.setPrefWidth(150);
        milestoneNodeBox.setAlignment(Pos.CENTER);
        Node milestoneNode = setMouseClickForNode(milestone.getNode(), milestone.getTitle());
        milestoneNodeBox.getChildren().add(milestoneNode);
        return milestoneNodeBox;
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
        milestoneBox.setPrefHeight(30);
        milestoneBox.setMaxHeight(30);
        milestoneBox.setStyle("-fx-border-radius: 3;-fx-border-style: dotted;-fx-alignment:center");
        return milestoneBox;
    }

    private Label createHiddenMilestonesIndicator(int hiddenMilestonesSize) {
        Label hiddenMilestonesIndicator = new Label("and " + hiddenMilestonesSize + " more...");
        hiddenMilestonesIndicator.setPadding(new Insets(3, 3, 3, 3));
        return hiddenMilestonesIndicator;
    }

    private Optional<PickerMilestone> getExistingMilestone(List<PickerMilestone> milestoneList) {
        return milestoneList.stream()
                .filter(PickerMilestone::isExisting)
                .findAny();
    }

    private boolean hasSelectedMilestone(List<PickerMilestone> milestoneList) {
        return milestoneList.stream()
                .filter(PickerMilestone::isSelected)
                .findAny()
                .isPresent();
    }

    private Optional<PickerMilestone> getSelectedMilestone(List<PickerMilestone> milestoneList) {
        return milestoneList.stream()
                .filter(PickerMilestone::isSelected)
                .findAny();
    }

    private Optional<PickerMilestone> getHighlightedMilestone(List<PickerMilestone> milestones) {
        return milestones.stream()
                .filter(PickerMilestone::isHighlighted)
                .findAny();
    }

}
