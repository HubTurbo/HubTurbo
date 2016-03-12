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

public class MilestonePickerDialog extends Dialog<Pair<ButtonType, Integer>> {
    private static final String OCTICON_ARROW = "\uf03e";
    private static final String DIALOG_TITLE = "Select Milestone";

    private final List<PickerMilestone> originalMilestones = new ArrayList<>();
    FlowPane assignedMilestone;
    VBox matchingMilestones;
    private TextField inputField;
    private MilestonePickerState state;

    /**
     * Constructor to create a MilestonePickerDialog
     *
     * The issue and the originalMilestones list provided should come from the same repository
     * @param stage
     * @param issue
     * @param milestones
     */
    public MilestonePickerDialog(Stage stage, TurboIssue issue, List<TurboMilestone> milestones) {
        initOwner(stage);
        setTitle(DIALOG_TITLE);
        setupButtons(getDialogPane());
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
        for (int i = 0; i < milestones.size(); i++) {
            PickerMilestone convertedMilestone = new PickerMilestone(milestones.get(i));
            if (isExistingMilestone(issue, convertedMilestone)) {
                convertedMilestone.setExisting(true);
            }
            originalMilestones.add(convertedMilestone);
        }

        Collections.sort(originalMilestones);
        selectAssignedMilestone(originalMilestones, issue);

        return originalMilestones;
    }

    private boolean isExistingMilestone(TurboIssue issue, PickerMilestone milestone) {
        if (!issue.getMilestone().isPresent()) return false;
        return issue.getMilestone().get() == milestone.getId();
    }

    private void selectAssignedMilestone(List<PickerMilestone> milestones, TurboIssue issue) {
        milestones.stream()
                .filter(milestone -> isExistingMilestone(issue, milestone))
                .forEach(milestone -> milestone.setSelected(true));
    }

    private void setupButtons(DialogPane milestonePickerDialogPane) {
        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        milestonePickerDialogPane.getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        setConfirmResultConverter();
    }

    private void setConfirmResultConverter() {
        setResultConverter((dialogButton) -> {
            List<PickerMilestone> finalList = state.getCurrentMilestonesList();
            if (hasSelectedMilestone(finalList)) {
                return new Pair<>(dialogButton, getSelectedMilestone(finalList).get().getId());
            }
            return new Pair<>(dialogButton, null);
        });
    }

    private Node setMouseClickForNode(Node node, String milestoneName) {
        node.setOnMouseClicked(e -> handleMouseClick(milestoneName));
        return node;
    }

    private void handleMouseClick(String milestoneName) {
        // required since clearing inputField will change the reference to the state
        MilestonePickerState curState = state;
        inputField.clear();
        inputField.setDisable(true);
        curState.toggleMilestone(milestoneName);
        state = curState;
        refreshUI(state);
    }

    private void initUI() {
        VBox milestoneBox = new VBox();
        milestoneBox.setSpacing(3);
        assignedMilestone = new FlowPane();
        assignedMilestone.setPadding(new Insets(5, 5, 5, 5));
        assignedMilestone.setStyle("-fx-alignment:center;");
        matchingMilestones = createMatchingMilestonesBox();
        inputField = new TextField();
        inputField.setId("milestonePickerTextField");

        milestoneBox.getChildren().add(assignedMilestone);
        milestoneBox.getChildren().add(inputField);
        milestoneBox.getChildren().add(matchingMilestones);

        getDialogPane().setContent(milestoneBox);
        Platform.runLater(inputField::requestFocus);
        refreshUI(state);
    }

    private void refreshUI(MilestonePickerState state) {
        List<PickerMilestone> milestonesToDisplay = state.getCurrentMilestonesList();
        populateAssignedMilestone(milestonesToDisplay, assignedMilestone);

        List<PickerMilestone> matchingMilestones = state.getMatchingMilestonesList();
        populateMatchingMilestones(matchingMilestones, this.matchingMilestones);
    }

    private void populateAssignedMilestone(List<PickerMilestone> pickerMilestoneList,
                                           FlowPane assignedMilestoneStatus) {
        assignedMilestoneStatus.getChildren().clear();
        updateExistingMilestones(pickerMilestoneList, assignedMilestoneStatus);
        addAssignmentIndicator(assignedMilestoneStatus);
        updateNewlyAddedMilestone(pickerMilestoneList, assignedMilestoneStatus);
    }

    private void populateMatchingMilestones(List<PickerMilestone> pickerMilestoneList, VBox matchingMilestones) {
        matchingMilestones.getChildren().clear();
        pickerMilestoneList.stream()
                .sorted()
                .limit(5)
                .forEach(milestone -> matchingMilestones.getChildren().add(createDetailedMilestoneBox(milestone)));

        if (pickerMilestoneList.size() <= 5) return;
        Label moreMilestonesIndicator = new Label("and " + (pickerMilestoneList.size() - 5) + " more...");
        moreMilestonesIndicator.setPadding(new Insets(3, 3, 3, 3));
        matchingMilestones.getChildren().add(moreMilestonesIndicator);
    }

    private void addAssignmentIndicator(FlowPane assignedMilestoneStatus) {
        Label separator = new Label(OCTICON_ARROW);
        separator.getStyleClass().add("octicon");
        separator.setPadding(new Insets(5, 5, 5, 5));
        assignedMilestoneStatus.getChildren().add(separator);
    }

    private void updateNewlyAddedMilestone(List<PickerMilestone> pickerMilestoneList, FlowPane assignedMilestoneStatus) {
        boolean hasSuggestion = hasHighlightedMilestone(pickerMilestoneList);
        HBox newlyAssignedMilestoneBox = createNewlyAssignedMilestoneBox();
        assignedMilestoneStatus.getChildren().add(newlyAssignedMilestoneBox);

        if (!hasSuggestion && !hasSelectedMilestone(pickerMilestoneList)) return;

        Node nodeToAdd = hasSuggestion
                ? getHighlightedMilestone(pickerMilestoneList).get().getNewlyAssignedMilestoneNode(hasSuggestion)
                : getSelectedMilestone(pickerMilestoneList).get().getNewlyAssignedMilestoneNode(hasSuggestion);
        newlyAssignedMilestoneBox.getChildren().add(nodeToAdd);
    }

    private void updateExistingMilestones(List<PickerMilestone> pickerMilestoneList, FlowPane assignedMilestoneStatus) {
        HBox assignedMilestoneBox = createPreviouslyAssignedMilestoneBox();
        if (!hasExistingMilestone(pickerMilestoneList)) {
            assignedMilestoneStatus.getChildren().add(assignedMilestoneBox);
            return;
        }
        PickerMilestone existingMilestone = getExistingMilestone(pickerMilestoneList).get();

        Node existingMilestoneNode = setMouseClickForNode(existingMilestone.getSimpleNode(),
                existingMilestone.getTitle());

        assignedMilestoneBox.getChildren().add(existingMilestoneNode);
        assignedMilestoneStatus.getChildren().add(assignedMilestoneBox);
    }

    private HBox createDetailedMilestoneBox(PickerMilestone milestone) {
        HBox detailedMilestoneBox = new HBox();
        detailedMilestoneBox.setSpacing(3);
        detailedMilestoneBox.setPadding(new Insets(3, 3, 3, 3));
        detailedMilestoneBox.setStyle("-fx-border-width: 0 0 1 0; -fx-border-style: solid;");

        HBox milestoneNodeBox = new HBox();
        milestoneNodeBox.setPrefWidth(150);
        milestoneNodeBox.setAlignment(Pos.CENTER);

        HBox milestoneDetailsBox = new HBox();
        milestoneDetailsBox.setSpacing(3);
        milestoneDetailsBox.setPrefWidth(250);
        milestoneDetailsBox.setAlignment(Pos.CENTER_RIGHT);

        Label separator = new Label("|");
        detailedMilestoneBox.getChildren().setAll(milestoneNodeBox, separator, milestoneDetailsBox);

        Node milestoneNode = setMouseClickForNode(milestone.getNode(), milestone.getTitle());
        milestoneNodeBox.getChildren().add(milestoneNode);


        if (milestone.getDueDate().isPresent()) {
            Label dueDate = new Label("Due on: " + milestone.getDueDate().get().toString());
            dueDate.setPrefWidth(150);
            milestoneDetailsBox.getChildren().add(dueDate);
        }
        int totalIssues = milestone.getOpenIssues() + milestone.getClosedIssues();
        double progressValue;
        if (totalIssues > 0) {
            progressValue = milestone.getClosedIssues() / (milestone.getOpenIssues() + milestone.getClosedIssues());
        } else {
            progressValue = 0;
        }
        MilestoneProgressBar progressBar = new MilestoneProgressBar(progressValue);
        Label progressLabel = new Label(String.format("%3.0f%%", progressValue*100));
        progressLabel.setPrefWidth(50);
        milestoneDetailsBox.getChildren().addAll(progressBar, progressLabel);

        return detailedMilestoneBox;
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

    private boolean hasExistingMilestone(List<PickerMilestone> milestoneList) {
        return milestoneList.stream()
                .filter(milestone -> milestone.isExisting())
                .findAny()
                .isPresent();
    }

    private Optional<PickerMilestone> getExistingMilestone(List<PickerMilestone> milestoneList) {
        return milestoneList.stream()
                .filter(milestone -> milestone.isExisting())
                .findAny();
    }

    private boolean hasSelectedMilestone(List<PickerMilestone> milestoneList) {
        return milestoneList.stream()
                .filter(milestone -> milestone.isSelected())
                .findAny()
                .isPresent();
    }

    private Optional<PickerMilestone> getSelectedMilestone(List<PickerMilestone> milestoneList) {
        return milestoneList.stream()
                .filter(milestone -> milestone.isSelected())
                .findAny();
    }

    private boolean hasHighlightedMilestone(List<PickerMilestone> milestones) {
        return milestones.stream()
                .filter(milestone -> milestone.isHighlighted())
                .findAny()
                .isPresent();
    }

    private Optional<PickerMilestone> getHighlightedMilestone(List<PickerMilestone> milestones) {
        return milestones.stream()
                .filter(milestone -> milestone.isHighlighted())
                .findAny();
    }

}
