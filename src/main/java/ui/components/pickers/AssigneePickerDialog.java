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
    private static final String TITLE_ASSIGNED_USER = "Assigned User";
    private static final String TITLE_ALL_USERS = "All Users";
    private static final String MESSAGE_NO_ASSIGNEE = "No assignee";
    private static final String MESSAGE_NO_MATCHES = "No users matched your query.";

    private final List<PickerAssignee> originalUsers = new ArrayList<>();
    private FlowPane assignedUserPane;
    private VBox matchingUsersBox;
    private TextField textField;
    private AssigneePickerState state;

    public AssigneePickerDialog(Stage stage, TurboIssue issue, List<TurboUser> users) {
        initOwner(stage);
        setTitle(TITLE_DIALOG);
        setupButtons(getDialogPane());
        setConfirmResultConverter();
        originalUsers.addAll(convertToPickerAssignees(issue, users));
        state = new AssigneePickerState(originalUsers);
        initUI();
        setupKeyEvents();
        fillTextFieldWithExistingAssignee();
        Platform.runLater(() -> positionDialog(stage));
    }

    private void fillTextFieldWithExistingAssignee() {
        PickerAssignee.getExistingAssignee(originalUsers)
                .map(PickerAssignee::getLoginName)
                .ifPresent(this::fillTextFieldWithUsername);
    }

    private void fillTextFieldWithUsername(String username) {
        textField.setText(username);
    }

    private void setupKeyEvents() {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            handleUpdatedInput(newValue);
        });
    }

    private final void positionDialog(Stage stage) {
        setX(stage.getX() + stage.getWidth() / 2);
        setY(stage.getY() + stage.getHeight() / 2 - getHeight() / 2);
    }

    private void handleUpdatedInput(String userInput) {
        state = new AssigneePickerState(originalUsers, userInput);
        refreshUI(state);
    }

    private void handleMouseClick(String username) {
        textField.setText(username);
        refreshUI(state);
    }

    private List<PickerAssignee> convertToPickerAssignees(TurboIssue issue, List<TurboUser> users) {
        return users.stream()
                .map(user -> {
                    PickerAssignee convertedAssignee = new PickerAssignee(user);
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
            List<PickerAssignee> finalUsers = state.getCurrentUsersList();
            Optional<PickerAssignee> selectedAssignee = PickerAssignee.getSelectedAssignee(finalUsers);
            return new AssigneePickerDialogResponse(dialogButton, selectedAssignee.map(PickerAssignee::getLoginName));
        });
    }

    private void initUI() {
        VBox assigneeDialogBox = new VBox();
        assignedUserPane = createAssignedUserPane();
        matchingUsersBox = createMatchingUsersBox();
        ScrollPane matchingUsersPane = createMatchingUsersPane();
        textField = createTextField();

        assigneeDialogBox.getChildren().addAll(new Label(TITLE_ASSIGNED_USER),
                assignedUserPane,
                                                textField, new Label(TITLE_ALL_USERS),
                                                matchingUsersPane);

        getDialogPane().setContent(assigneeDialogBox);
        refreshUI(state);
        Platform.runLater(textField::requestFocus);
    }

    private void refreshUI(AssigneePickerState state) {
        populateAssignedUser(state.getCurrentUsersList(), assignedUserPane);
        populateMatchingUsers(state.getMatchingUsersList(), matchingUsersBox);
    }

    private void populateAssignedUser(List<PickerAssignee> users, FlowPane assignedUserPane) {
        assignedUserPane.getChildren().clear();

        updateExistingAssignee(users, assignedUserPane);
        addSeparator(assignedUserPane);
        updateNewlyAddedAssignee(users, assignedUserPane);
    }

    private void populateMatchingUsers(List<PickerAssignee> matchingUsers, VBox matchingUsersBox) {
        matchingUsersBox.getChildren().clear();

        if (matchingUsers.isEmpty()) {
            Label noMatchingUsersLabel = createNoMatchingUsersLabel();
            matchingUsersBox.getChildren().add(noMatchingUsersLabel);
            return; 
        }

        matchingUsers.stream()
                .sorted()
                .forEach(user -> matchingUsersBox.getChildren().add(setMouseClickForNode(
                        user.getMatchingNode(), user.getLoginName())));
    }

    private void addSeparator(FlowPane assignedUserPane) {
        assignedUserPane.getChildren().add(new Label("|"));
    }

    private void updateNewlyAddedAssignee(List<PickerAssignee> users, FlowPane assignedUserPane) {
        users.stream()
                .filter(PickerAssignee::isSelected)
                .forEach(user -> assignedUserPane.getChildren().add(
                        setMouseClickForNode(user.getNewlyAssignedAssigneeNode(),
                                user.getLoginName())
                ));
    }

    private void updateExistingAssignee(List<PickerAssignee> users, FlowPane assignedUserPane) {
        Optional<PickerAssignee> existingAssignee = PickerAssignee.getExistingAssignee(users);
        Node existingAssigneeNode = createExistingAssigneeNode(existingAssignee);
        assignedUserPane.getChildren().add(existingAssigneeNode);
    }

    private Node createExistingAssigneeNode(Optional<PickerAssignee> existingAssignee) {
        if (!existingAssignee.isPresent()) {
            return createNoExistingAssigneeLabel();
        }

        return setMouseClickForNode(existingAssignee.get().getExistingAssigneeNode(),
                existingAssignee.get().getLoginName());
    }

    private Node setMouseClickForNode(Node node, String username) {
        node.setOnMouseClicked(e -> handleMouseClick(username));
        return node;
    }

    private FlowPane createAssignedUserPane() {
        FlowPane assignedUserPane = new FlowPane();
        assignedUserPane.setPadding(new Insets(5, 5, 5, 5));
        assignedUserPane.setHgap(3);
        assignedUserPane.setVgap(5);
        assignedUserPane.setStyle("-fx-border-radius: 3;");
        return assignedUserPane;
    }

    private VBox createMatchingUsersBox() {
        VBox matchingUsersBox = new VBox();
        matchingUsersBox.setMinHeight(198);
        matchingUsersBox.setStyle("-fx-background-color: white;");
        return matchingUsersBox;
    }

    private ScrollPane createMatchingUsersPane() {
        ScrollPane matchingUsersPane = new ScrollPane();
        matchingUsersPane.setMaxHeight(200);
        matchingUsersPane.setMinHeight(200);
        matchingUsersPane.setContent(matchingUsersBox);
        return matchingUsersPane;
    }

    private TextField createTextField() {
        TextField textField = new TextField();
        textField.setId(ID_TEXT_FIELD);
        return textField;
    }

    private Label createNoMatchingUsersLabel() {
        Label noMatchingUsersLabel = new Label(MESSAGE_NO_MATCHES);
        noMatchingUsersLabel.setPrefHeight(40);
        noMatchingUsersLabel.setPrefWidth(398);
        noMatchingUsersLabel.setAlignment(Pos.CENTER);
        return noMatchingUsersLabel;
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
