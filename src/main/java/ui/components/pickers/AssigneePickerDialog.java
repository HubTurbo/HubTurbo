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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AssigneePickerDialog extends Dialog<AssigneePickerDialogResponse> {
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

        getExistingAssignee(originalAssignees)
                .map(PickerAssignee::getLoginName)
                .ifPresent(this::fillTextFieldWithAssigneeLoginName);
    }

    private void fillTextFieldWithAssigneeLoginName(String assigneeLoginName) {
        textField.setText(assigneeLoginName);
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
        textField.setText(assigneeName);
        refreshUI(state);
    }

    private List<PickerAssignee> convertToPickerAssignees(TurboIssue issue, List<TurboUser> assignees) {
        List<PickerAssignee> originalAssignees = new ArrayList<>();
        assignees.stream()
        .forEach(assignee -> {
            PickerAssignee convertedAssignee = new PickerAssignee(assignee);
            if (isExistingAssignee(issue, convertedAssignee)) {
                convertedAssignee.setExisting(true);
            }
            originalAssignees.add(convertedAssignee);
        });

        Collections.sort(originalAssignees);

        return originalAssignees;
    }

    private boolean isExistingAssignee(TurboIssue issue, PickerAssignee assignee) {
        if (!issue.getAssignee().isPresent()) return false;
        return issue.getAssignee().get().equals(assignee.getLoginName());
    }

    private void setupButtons(DialogPane assigneePickerDialogPane) {
        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        assigneePickerDialogPane.getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
    }

    private void setConfirmResultConverter() {
        setResultConverter((dialogButton) -> {
            List<PickerAssignee> finalList = state.getCurrentAssigneesList();
            if (hasSelectedAssignee(finalList)) {
                return new AssigneePickerDialogResponse(dialogButton,
                        Optional.of(getSelectedAssignee(finalList).get().getLoginName()));
            }
            return new AssigneePickerDialogResponse(dialogButton, Optional.empty());
        });
    }

    private void initUI() {
        VBox assigneeDialogBox = new VBox();
        assignedAssigneePane = createAssignedAssigneeGroup();
        matchingAssigneesBox = createMatchingAssigneeBox();
        matchingAssigneePane = createMatchingAssigneePane();
        textField = createTextField();

        assigneeDialogBox.getChildren().add(new Label(ASSIGNED_ASSIGNEE));
        assigneeDialogBox.getChildren().add(assignedAssigneePane);
        assigneeDialogBox.getChildren().add(textField);
        assigneeDialogBox.getChildren().add(new Label(ALL_ASSIGNEES));
        assigneeDialogBox.getChildren().add(matchingAssigneePane);


        getDialogPane().setContent(assigneeDialogBox);
        refreshUI(state);
        Platform.runLater(textField::requestFocus);
    }

    private void refreshUI(AssigneePickerState state) {
        populateAssignedAssignee(state.getCurrentAssigneesList(), assignedAssigneePane);
        populateMatchingAssignee(state.getMatchingAssigneeList(), matchingAssigneesBox);
    }

    private void populateAssignedAssignee(List<PickerAssignee> assigneeList, FlowPane assignedAssigneeStatus) {
        assignedAssigneeStatus.getChildren().clear();
        boolean hasSelectedAssignee = hasSelectedAssignee(assigneeList);

        updateExistingAssignee(assigneeList, assignedAssigneeStatus, hasSelectedAssignee);
        addSeparator(assignedAssigneeStatus);
        updateNewlyAddedAssignee(assigneeList, assignedAssigneeStatus);
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
                .forEach(assignee -> matchingAssigneesBox.getChildren().add(setMouseClickForNode(
                        assignee.getMatchingNode(), assignee.getLoginName())));
    }

    private void addSeparator(FlowPane assignedAssigneeStatus) {
        assignedAssigneeStatus.getChildren().add(new Label("|"));
    }

    private void updateNewlyAddedAssignee(List<PickerAssignee> assigneeList, FlowPane assignedAssigneeStatus) {
        assigneeList.stream()
                .filter(assignee -> assignee.isSelected())
                .forEach(assignee -> assignedAssigneeStatus.getChildren().add(
                        setMouseClickForNode(assignee.getNewlyAssignedAssigneeNode(),
                                assignee.getLoginName())
                ));
    }

    private void updateExistingAssignee(List<PickerAssignee> assigneeList,
                                   FlowPane assignedAssigneeStatus, boolean hasSuggestion) {
        Optional<PickerAssignee> existingAssignee = getExistingAssignee(assigneeList);
        if (!existingAssignee.isPresent()) {
            Label noExistingAssignee = createNoExistingAssigneeLabel();
            assignedAssigneeStatus.getChildren().add(noExistingAssignee);
            return;
        }

        Node existingAssigneeNode = setMouseClickForNode(existingAssignee.get().getExistingAssigneeNode(hasSuggestion),
                    existingAssignee.get().getLoginName());
            assignedAssigneeStatus.getChildren().add(existingAssigneeNode);
    }

    private Node setMouseClickForNode(Node node, String assigneeName) {
        node.setOnMouseClicked(e -> handleMouseClick(assigneeName));
        return node;
    }

    private boolean hasSelectedAssignee(List<PickerAssignee> assigneeList) {
        return assigneeList.stream()
                .filter(PickerAssignee::isSelected)
                .findAny()
                .isPresent();
    }

    private Optional<PickerAssignee> getSelectedAssignee(List<PickerAssignee> assigneeList) {
        return assigneeList.stream()
                .filter(PickerAssignee::isSelected)
                .findAny();
    }

    private Optional<PickerAssignee> getExistingAssignee(List<PickerAssignee> assigneeList) {
        return assigneeList.stream()
                .filter(PickerAssignee::isExisting)
                .findAny();
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
        VBox assigneeMatchingBox = new VBox();
        assigneeMatchingBox.setMinHeight(198);
        assigneeMatchingBox.setStyle("-fx-background-color: white;");
        return assigneeMatchingBox;
    }

    private ScrollPane createMatchingAssigneePane() {
        ScrollPane matchingAssigneePane = new ScrollPane();
        matchingAssigneePane.setMaxHeight(200);
        matchingAssigneePane.setMinHeight(200);
        matchingAssigneePane.setContent(matchingAssigneesBox);
        return matchingAssigneePane;
    }

    private TextField createTextField() {
        TextField textField = new TextField();
        textField.setId("assigneePickerTextField");
        return textField;
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

}
