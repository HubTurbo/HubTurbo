package ui.components.pickers;

import backend.resource.TurboIssue;
import backend.resource.TurboUser;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AssigneePickerDialog extends Dialog<Pair<ButtonType, String>> {
    public static final String DIALOG_TITLE = "Select Assignee";
    private static final String ASSIGNED_ASSIGNEE = "Assigned Assignee";
    private static final String ALL_ASSIGNEES = "All Assignees";

    private final List<PickerAssignee> originalAssignees = new ArrayList<>();
    FlowPane allAssigneesPane, assignedAssigneePane;
    private TextField textField;
    private AssigneePickerState state;

    public AssigneePickerDialog(Stage stage, TurboIssue issue, List<TurboUser> assignees) {
        initOwner(stage);
        setTitle(DIALOG_TITLE);
        setupButtons(getDialogPane());
        setConfirmResultConverter();
        originalAssignees.addAll(convertToPickerAssignees(issue, assignees));
        state = new AssigneePickerState(originalAssignees);
        initUI();
        setupKeyEvents();
    }

    private void handleMouseClick(String assigneeName) {
        // required since clearing inputField will change the reference to the state
        AssigneePickerState curState = state;
        textField.clear();
        textField.setDisable(true);
        curState.toggleAssignee(assigneeName);
        state = curState;
        refreshUI(state);
    }

    private void setupKeyEvents() {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            handleUpdatedInput(newValue);
        });
    }

    private void handleUpdatedInput(String userInput) {
        state = new AssigneePickerState(originalAssignees, userInput);
        refreshUI(state);
    }

    private boolean hasHighlightedAssignee(List<PickerAssignee> assigneeList) {
        return assigneeList.stream()
                .filter(assignee -> assignee.isHighlighted())
                .findAny()
                .isPresent();
    }

    private List<PickerAssignee> convertToPickerAssignees(TurboIssue issue, List<TurboUser> assignees) {
        List<PickerAssignee> originalAssignees = new ArrayList<>();
        for (int i = 0; i < assignees.size(); i++) {
            PickerAssignee convertedAssignee = new PickerAssignee(assignees.get(i));
            if (isExistingAssignee(issue, convertedAssignee)) {
                convertedAssignee.setExisting(true);
            }
            originalAssignees.add(convertedAssignee);
        }

        Collections.sort(originalAssignees);
        selectAssignedAssignee(originalAssignees, issue);

        return originalAssignees;
    }

    private boolean isExistingAssignee(TurboIssue issue, PickerAssignee assignee) {
        if (issue.getAssignee().isPresent()) {
            return issue.getAssignee().get().equals(assignee.getLoginName());
        } else {
            return false;
        }
    }

    private void selectAssignedAssignee(List<PickerAssignee> assigneeList, TurboIssue issue) {
        assigneeList.stream()
                .filter(assignee -> isExistingAssignee(issue, assignee))
                .forEach(assignee -> assignee.setSelected(true));
    }

    private void setupButtons(DialogPane assigneePickerDialogPane) {
        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        assigneePickerDialogPane.getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
    }

    private void setConfirmResultConverter() {
        setResultConverter((dialogButton) -> {
            List<PickerAssignee> finalList = state.getCurrentAssigneesList();
            if (hasSelectedAssignee(finalList)) {
                return new Pair<>(dialogButton, getSelectedAssignee(finalList).getLoginName());
            } else {
                return new Pair<>(dialogButton, null);
            }
        });
    }

    private void initUI() {
        VBox assigneeBox = new VBox();
        assignedAssigneePane = createAssigneeGroup();
        allAssigneesPane = createAssigneeGroup();
        textField = new TextField();

        assigneeBox.getChildren().add(new Label(ASSIGNED_ASSIGNEE));
        assigneeBox.getChildren().add(assignedAssigneePane);
        assigneeBox.getChildren().add(new Label(ALL_ASSIGNEES));
        assigneeBox.getChildren().add(allAssigneesPane);
        assigneeBox.getChildren().add(textField);

        getDialogPane().setContent(assigneeBox);
        Platform.runLater(textField::requestFocus);
        refreshUI(state);
    }

    private void refreshUI(AssigneePickerState state) {
        List<PickerAssignee> assigneesToDisplay = state.getCurrentAssigneesList();
        populateAssignedAssignee(assigneesToDisplay, assignedAssigneePane);
        populateAllAssignees(assigneesToDisplay, allAssigneesPane);
    }

    private void populateAssignedAssignee(List<PickerAssignee> assigneeList, FlowPane assignedAssigneeStatus) {
        assignedAssigneeStatus.getChildren().clear();
        boolean hasSuggestion = hasHighlightedAssignee(assigneeList);

        updateExistingAssignee(assigneeList, assignedAssigneeStatus, hasSuggestion);
        addSeparator(assignedAssigneeStatus);
        updateNewlyAddedAssignee(assigneeList, assignedAssigneeStatus, hasSuggestion);
        updateSuggestedAssignee(assigneeList, assignedAssigneeStatus, hasSuggestion);
    }

    private void addSeparator(FlowPane assignedAssigneeStatus) {
        assignedAssigneeStatus.getChildren().add(new Label("|"));
    }

    private void updateSuggestedAssignee(List<PickerAssignee> assigneeList,
                                         FlowPane assignedAssigneeStatus, boolean hasSuggestion) {
        assigneeList.stream()
                .filter(assignee -> !assignee.isExisting() && assignee.isHighlighted() && !assignee.isSelected())
                .forEach(assignee -> assignedAssigneeStatus.getChildren().add(
                        setMouseClickForNode(assignee.getNewlyAssignedAssigneeNode(hasSuggestion),
                                assignee.getLoginName())
                ));
    }

    private void updateNewlyAddedAssignee(List<PickerAssignee> assigneeList,
                                           FlowPane assignedAssigneeStatus, boolean hasSuggestion) {
        assigneeList.stream()
                .filter(assignee -> assignee.isSelected() && !assignee.isExisting())
                .forEach(assignee -> assignedAssigneeStatus.getChildren().add(
                        setMouseClickForNode(assignee.getNewlyAssignedAssigneeNode(hasSuggestion),
                                assignee.getLoginName())
                ));
    }

    private void updateExistingAssignee(List<PickerAssignee> assigneeList,
                                   FlowPane assignedAssigneeStatus, boolean hasSuggestion) {
        if (hasExistingAssignee(assigneeList)) {
            PickerAssignee existingAssignee = getExistingAssignee(assigneeList);

            Node existingAssigneeNode = setMouseClickForNode(existingAssignee.getExistingAssigneeNode(hasSuggestion),
                    existingAssignee.getLoginName());
            assignedAssigneeStatus.getChildren().add(existingAssigneeNode);
        }
    }

    private boolean hasExistingAssignee(List<PickerAssignee> assigneeList) {
        return assigneeList.stream()
                .filter(assignee -> assignee.isExisting())
                .findAny()
                .isPresent();
    }

    private PickerAssignee getExistingAssignee(List<PickerAssignee> assigneeList) {
        return assigneeList.stream()
                .filter(assignee -> assignee.isExisting())
                .findAny()
                .get();
    }

    private void populateAllAssignees(List<PickerAssignee> assigneeList, FlowPane allAssigneesPane) {
        allAssigneesPane.getChildren().clear();
        assigneeList.stream()
                .forEach(assignee -> allAssigneesPane.getChildren().add(setMouseClickForNode(assignee.getNode(),
                        assignee.getLoginName())));
    }

    private Node setMouseClickForNode(Node node, String assigneeName) {
        node.setOnMouseClicked(e -> handleMouseClick(assigneeName));
        return node;
    }

    private FlowPane createAssigneeGroup() {
        FlowPane assigneeGroup = new FlowPane();
        assigneeGroup.setPadding(new Insets(3));
        assigneeGroup.setHgap(3);
        assigneeGroup.setVgap(3);
        assigneeGroup.setStyle("-fx-border-radius: 3;-fx-background-color: white;-fx-border-color: black;");
        return assigneeGroup;
    }

    private boolean hasSelectedAssignee(List<PickerAssignee> assigneeList) {
        return assigneeList.stream()
                .filter(assignee -> assignee.isSelected())
                .findAny()
                .isPresent();
    }

    private PickerAssignee getSelectedAssignee(List<PickerAssignee> assigneeList) {
        return assigneeList.stream()
                .filter(assignee -> assignee.isSelected())
                .findAny()
                .get();
    }
}
