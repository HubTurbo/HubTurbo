package ui.components.pickers;

import backend.resource.TurboIssue;
import backend.resource.TurboUser;
import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
    FlowPane assignedAssigneePane;
    ScrollPane matchingAssigneePane;
    VBox matchingAssigneesBox;
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

    private void setupKeyEvents() {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            handleUpdatedInput(newValue);
        });
    }

    private void handleUpdatedInput(String userInput) {
        state = new AssigneePickerState(originalAssignees, userInput);
        refreshUI(state);
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
        if (!issue.getAssignee().isPresent()) return false;
        return issue.getAssignee().get().equals(assignee.getLoginName());
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
        assignedAssigneePane = createAssignedAssigneeGroup();
        matchingAssigneesBox = createMatchingAssigneeBox();
        matchingAssigneePane = createMatchingAssigneePane();
        textField = new TextField();

        assigneeBox.getChildren().add(new Label(ASSIGNED_ASSIGNEE));
        assigneeBox.getChildren().add(assignedAssigneePane);
        assigneeBox.getChildren().add(textField);
        assigneeBox.getChildren().add(new Label(ALL_ASSIGNEES));
        assigneeBox.getChildren().add(matchingAssigneePane);


        getDialogPane().setContent(assigneeBox);
        Platform.runLater(textField::requestFocus);
        refreshUI(state);
    }

    private void refreshUI(AssigneePickerState state) {
        populateAssignedAssignee(state.getCurrentAssigneesList(), assignedAssigneePane);
        populateMatchingAssignee(state.getMatchingAssigneeList(), matchingAssigneesBox);
    }

    private void populateAssignedAssignee(List<PickerAssignee> assigneeList, FlowPane assignedAssigneeStatus) {
        assignedAssigneeStatus.getChildren().clear();
        boolean hasSuggestion = hasHighlightedAssignee(assigneeList);

        updateExistingAssignee(assigneeList, assignedAssigneeStatus, hasSuggestion);
        addSeparator(assignedAssigneeStatus);
        updateNewlyAddedAssignee(assigneeList, assignedAssigneeStatus, hasSuggestion);
        updateSuggestedAssignee(assigneeList, assignedAssigneeStatus, hasSuggestion);
    }

    private void populateMatchingAssignee(List<PickerAssignee> matchingAssigneeList, VBox matchingAssigneesBox) {
        matchingAssigneesBox.getChildren().clear();

        if (matchingAssigneeList.isEmpty()) {
            Label noMatchAssigneeLabel = createNoMatchingAssigneeLabel();
            matchingAssigneesBox.getChildren().add(noMatchAssigneeLabel);
            return; 
        }

        matchingAssigneeList.stream()
                .sorted()
                .forEach(assignee -> matchingAssigneesBox.getChildren().add(setMouseClickForNode(assignee.getHNode(),
                        assignee.getLoginName())));
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
        } else {
            Label noExistingAssignee = createNoExistingAssigneeLabel();
            assignedAssigneeStatus.getChildren().add(noExistingAssignee);
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

    private Node setMouseClickForNode(Node node, String assigneeName) {
        node.setOnMouseClicked(e -> handleMouseClick(assigneeName));
        return node;
    }

    private FlowPane createAssignedAssigneeGroup() {
        FlowPane assigneeGroup = new FlowPane();
        assigneeGroup.setPadding(new Insets(5, 5, 5, 5));
        assigneeGroup.setHgap(3);
        assigneeGroup.setVgap(5);
        assigneeGroup.setStyle("-fx-border-radius: 3;");
        return assigneeGroup;
    }

    private VBox createMatchingAssigneeBox() {
        VBox milestoneGroup = new VBox();
        milestoneGroup.setStyle("-fx-background-color: white;");
        return milestoneGroup;
    }

    private ScrollPane createMatchingAssigneePane() {
        ScrollPane matchingAssigneePane = new ScrollPane();
        matchingAssigneePane.setMaxHeight(200);
        matchingAssigneePane.setContent(matchingAssigneesBox);
        return matchingAssigneePane;
    }

    private Label createNoMatchingAssigneeLabel() {
        Label noMatchAssigneeLabel = new Label("No users matched your query.");
        noMatchAssigneeLabel.setPrefHeight(40);
        noMatchAssigneeLabel.setPrefWidth(398);
        noMatchAssigneeLabel.setAlignment(Pos.CENTER);
        return noMatchAssigneeLabel;
    }

    private Label createNoExistingAssigneeLabel() {
        Label noExistingAssignee = new Label("No assignee");
        noExistingAssignee.setPrefHeight(40);
        FontLoader fontLoader = Toolkit.getToolkit().getFontLoader();
        double width = fontLoader.computeStringWidth(noExistingAssignee.getText(), noExistingAssignee.getFont());
        noExistingAssignee.setPrefWidth(width);
        return noExistingAssignee;
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
