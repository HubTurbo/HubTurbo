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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AssigneePickerDialog extends Dialog<AssigneePickerDialog.AssigneePickerDialogResponse> {
    private static final String ID_TEXT_FIELD = "assigneePickerTextField";
    private static final String TITLE_DIALOG = "Select Assignee";
    private static final String TITLE_ASSIGNED_ASSIGNEE = "Assigned Assignee";
    private static final String TITLE_ALL_ASSIGNEES = "All Assignees";
    private static final String MESSAGE_NO_ASSIGNEE = "No assignee";
    private static final String MESSAGE_NO_MATCHES = "No users matched your query.";

    private final List<PickerAssignee> originalAssignees = new ArrayList<>();
    private FlowPane assignedAssigneePane;
    private VBox matchingAssigneesBox;
    private TextField textField;
    private AssigneePickerState state;

    public AssigneePickerDialog(Stage stage, TurboIssue issue, List<TurboUser> assignees) {
        initOwner(stage);
        setTitle(TITLE_DIALOG);
        setupButtons(getDialogPane());
        setConfirmResultConverter();
        originalAssignees.addAll(convertToPickerAssignees(issue, assignees));
        state = new AssigneePickerState(originalAssignees);
        initUI();
        setupKeyEvents();
        fillTextFieldWithExistingAssignee();
    }

    private void fillTextFieldWithExistingAssignee() {
        PickerAssignee.getExistingAssignee(originalAssignees)
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
        return assignees.stream()
                .map(assignee -> {
                    PickerAssignee convertedAssignee = new PickerAssignee(assignee);
                    if (isExistingAssignee(issue, convertedAssignee)) {
                        convertedAssignee.setExisting(true);
                    }
                    return convertedAssignee;
                }).sorted().collect(Collectors.toList());
    }

    private boolean isExistingAssignee(TurboIssue issue, PickerAssignee assignee) {
        if (!issue.getAssignee().isPresent()) return false;
        return issue.getAssignee().get().equals(assignee.getLoginName());
    }

    private void setupButtons(DialogPane dialogPane) {
        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
    }

    private void setConfirmResultConverter() {
        setResultConverter((dialogButton) -> {
            List<PickerAssignee> finalAssignees = state.getCurrentAssigneesList();
            Optional<PickerAssignee> selectedAssignee = PickerAssignee.getSelectedAssignee(finalAssignees);
            return new AssigneePickerDialogResponse(dialogButton, selectedAssignee.map(PickerAssignee::getLoginName));
        });
    }

    private void initUI() {
        VBox assigneeDialogBox = new VBox();
        assignedAssigneePane = createAssignedAssigneeGroup();
        matchingAssigneesBox = createMatchingAssigneeBox();
        ScrollPane matchingAssigneePane = createMatchingAssigneePane();
        textField = createTextField();

        assigneeDialogBox.getChildren().addAll(new Label(TITLE_ASSIGNED_ASSIGNEE),
                                                assignedAssigneePane,
                                                textField, new Label(TITLE_ALL_ASSIGNEES),
                                                matchingAssigneePane);

        getDialogPane().setContent(assigneeDialogBox);
        refreshUI(state);
        Platform.runLater(textField::requestFocus);
    }

    private void refreshUI(AssigneePickerState state) {
        populateAssignedAssignee(state.getCurrentAssigneesList(), assignedAssigneePane);
        populateMatchingAssignee(state.getMatchingAssigneesList(), matchingAssigneesBox);
    }

    private void populateAssignedAssignee(List<PickerAssignee> assignees, FlowPane assignedAssigneeStatus) {
        assignedAssigneeStatus.getChildren().clear();
        boolean hasSelectedAssignee = PickerAssignee.getSelectedAssignee(assignees).isPresent();

        updateExistingAssignee(assignees, assignedAssigneeStatus, hasSelectedAssignee);
        addSeparator(assignedAssigneeStatus);
        updateNewlyAddedAssignee(assignees, assignedAssigneeStatus);
    }

    private void populateMatchingAssignee(List<PickerAssignee> matchingAssignees, VBox matchingAssigneesBox) {
        matchingAssigneesBox.getChildren().clear();

        if (matchingAssignees.isEmpty()) {
            Label noMatchAssigneeLabel = createNoMatchingAssigneeLabel();
            matchingAssigneesBox.getChildren().add(noMatchAssigneeLabel);
            return; 
        }

        matchingAssignees.stream()
                .sorted()
                .forEach(assignee -> matchingAssigneesBox.getChildren().add(setMouseClickForNode(
                        assignee.getMatchingNode(), assignee.getLoginName())));
    }

    private void addSeparator(FlowPane assignedAssigneeStatus) {
        assignedAssigneeStatus.getChildren().add(new Label("|"));
    }

    private void updateNewlyAddedAssignee(List<PickerAssignee> assignees, FlowPane assignedAssigneeStatus) {
        assignees.stream()
                .filter(PickerAssignee::isSelected)
                .forEach(assignee -> assignedAssigneeStatus.getChildren().add(
                        setMouseClickForNode(assignee.getNewlyAssignedAssigneeNode(),
                                assignee.getLoginName())
                ));
    }

    private void updateExistingAssignee(List<PickerAssignee> assignees,
                                   FlowPane assignedAssigneeStatus, boolean hasSuggestion) {
        Optional<PickerAssignee> existingAssignee = PickerAssignee.getExistingAssignee(assignees);
        Node existingAssigneeNode = createExistingAssigneeNode(existingAssignee, hasSuggestion);
        assignedAssigneeStatus.getChildren().add(existingAssigneeNode);
    }

    private Node createExistingAssigneeNode(Optional<PickerAssignee> existingAssignee, boolean hasSuggestion) {
        if (!existingAssignee.isPresent()) {
            return createNoExistingAssigneeLabel();
        }

        return setMouseClickForNode(existingAssignee.get().getExistingAssigneeNode(hasSuggestion),
                existingAssignee.get().getLoginName());
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
        textField.setId(ID_TEXT_FIELD);
        return textField;
    }

    private Label createNoMatchingAssigneeLabel() {
        Label noMatchAssigneeLabel = new Label(MESSAGE_NO_MATCHES);
        noMatchAssigneeLabel.setPrefHeight(40);
        noMatchAssigneeLabel.setPrefWidth(398);
        noMatchAssigneeLabel.setAlignment(Pos.CENTER);
        return noMatchAssigneeLabel;
    }

    private Label createNoExistingAssigneeLabel() {
        Label noExistingAssignee = new Label(MESSAGE_NO_ASSIGNEE);
        noExistingAssignee.setPrefHeight(40);
        FontLoader fontLoader = Toolkit.getToolkit().getFontLoader();
        double width = fontLoader.computeStringWidth(noExistingAssignee.getText(), noExistingAssignee.getFont());
        noExistingAssignee.setPrefWidth(width);
        return noExistingAssignee;
    }

    public static class AssigneePickerDialogResponse {
        private final ButtonType buttonClicked;
        private final Optional<String> assigneeLoginName;

        public AssigneePickerDialogResponse(ButtonType buttonClicked, Optional<String> assigneeLoginName) {
            this.buttonClicked = buttonClicked;
            this.assigneeLoginName = assigneeLoginName;
        }

        public ButtonType getButtonClicked() {
            return buttonClicked;
        }

        public Optional<String> getAssigneeLoginName() {
            return assigneeLoginName;
        }
    }

}
