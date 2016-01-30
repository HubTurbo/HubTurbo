package ui.components.pickers;

import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ui.UI;

public class LabelPickerDialog extends Dialog<List<String>> {

    private static final int ELEMENT_MAX_WIDTH = 108;

    private final LabelPickerUILogic uiLogic;

    @FXML
    private VBox mainLayout;
    @FXML
    private Label title;
    @FXML
    private FlowPane assignedLabels;
    @FXML
    private TextField queryField;
    @FXML
    private VBox feedbackLabels;

    LabelPickerDialog(TurboIssue issue, List<TurboLabel> repoLabels, Stage stage) {
        // UI creation
        initUI(stage, issue);
        setupEvents(stage);
        uiLogic = new LabelPickerUILogic(issue, repoLabels, this);
        Platform.runLater(queryField::requestFocus);
    }

    private void setupEvents(Stage stage) {
        setupKeyEvents();

        showingProperty().addListener(e -> {
            positionDialog(stage);
        });
    }

    private void setupKeyEvents() {
        queryField.textProperty().addListener((observable, oldValue, newValue) -> {
            uiLogic.processTextFieldChange(newValue.toLowerCase());
        });
        queryField.setOnKeyPressed(e -> {
            if (!e.isAltDown() && !e.isMetaDown() && !e.isControlDown()) {
                if (e.getCode() == KeyCode.DOWN) {
                    e.consume();
                    uiLogic.moveHighlightOnLabel(true);
                } else if (e.getCode() == KeyCode.UP) {
                    e.consume();
                    uiLogic.moveHighlightOnLabel(false);
                } else if (e.getCode() == KeyCode.SPACE) {
                    e.consume();
                    uiLogic.toggleSelectedLabel(queryField.getText());
                }
            }
        });
    }

    @SuppressWarnings("unused")
    private void ______POPULATION______() {}

    protected void populatePanes(List<PickerLabel> existingLabels, List<PickerLabel> newTopLabels,
            List<PickerLabel> bottomLabels, Map<String, Boolean> groups) {
        populateTopPane(existingLabels, newTopLabels);
        populateBottomBox(bottomLabels, groups);
    }

    private void populateTopPane(List<PickerLabel> existingLabels, List<PickerLabel> newTopLabels) {
        assignedLabels.getChildren().clear();
        if (existingLabels.isEmpty() && newTopLabels.isEmpty()) {
            Label label = new Label("No currently selected labels. ");
            label.setPadding(new Insets(2, 5, 2, 5));
            assignedLabels.getChildren().add(label);
        } else {
            existingLabels.forEach(label -> assignedLabels.getChildren().add(label.getNode()));
            if (!newTopLabels.isEmpty()) {
                assignedLabels.getChildren().add(new Label("|"));
                newTopLabels.forEach(label -> assignedLabels.getChildren().add(label.getNode()));
            }
        }
    }

    private void populateBottomBox(List<PickerLabel> bottomLabels, Map<String, Boolean> groups) {
        feedbackLabels.getChildren().clear();
        if (bottomLabels.isEmpty()) {
            Label label = new Label("No labels in repository. ");
            label.setPadding(new Insets(2, 5, 2, 5));
            feedbackLabels.getChildren().add(label);
        } else {
            List<String> groupNames = groups.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
            Collections.sort(groupNames, String.CASE_INSENSITIVE_ORDER);

            groupNames.stream().forEach(group -> {
                Label groupName = new Label(group + (groups.get(group) ? "." : "-"));
                groupName.setPadding(new Insets(0, 5, 5, 0));
                groupName.setMaxWidth(ELEMENT_MAX_WIDTH - 10);
                groupName.setStyle("-fx-font-size: 110%; -fx-font-weight: bold;");

                FlowPane groupPane = new FlowPane();
                groupPane.setHgap(5);
                groupPane.setVgap(5);
                groupPane.setPadding(new Insets(0, 0, 10, 10));
                bottomLabels
                        .stream()
                        .filter(label -> label.getGroup().isPresent())
                        .filter(label -> label.getGroup().get().equalsIgnoreCase(group))
                        .forEach(label -> groupPane.getChildren().add(label.getNode()));
                feedbackLabels.getChildren().addAll(groupName, groupPane);
            });

            FlowPane noGroup = new FlowPane();
            noGroup.setHgap(5);
            noGroup.setVgap(5);
            noGroup.setPadding(new Insets(5, 0, 0, 0));
            bottomLabels
                    .stream()
                    .filter(label -> !label.getGroup().isPresent())
                    .forEach(label -> noGroup.getChildren().add(label.getNode()));
            if (noGroup.getChildren().size() > 0) feedbackLabels.getChildren().add(noGroup);
        }
    }

    // UI creation

    private void initialiseDialog(Stage stage, TurboIssue issue) {
        initOwner(stage);
        initModality(Modality.APPLICATION_MODAL); // TODO change to NONE for multiple dialogs
        setTitle("Edit Labels for " + (issue.isPullRequest() ? "PR #" : "Issue #") +
                issue.getId() + " in " + issue.getRepoId());
    }

    public final void positionDialog(Stage stage) {
        if (!Double.isNaN(getHeight())) {
            setX(stage.getX() + stage.getScene().getX());
            setY(stage.getY() +
                 stage.getScene().getY() +
                 (stage.getScene().getHeight() - getHeight()) / 2);
        }
    }


    private void initUI(Stage stage, TurboIssue issue) {
        initialiseDialog(stage, issue);
        setDialogPaneContent();
        title.setTooltip(createTitleTooltip(issue));
        createButtons();
    }

    private void setDialogPaneContent() {
        try {
            createMainLayout();
            getDialogPane().setContent(mainLayout);
        } catch (IOException e) {
            // TODO use a HTLogger instead when failed to load fxml
            e.printStackTrace();
        }
    }

    private void createMainLayout() throws IOException {
        FXMLLoader loader = new FXMLLoader(UI.class.getResource("fxml/LabelPickerView.fxml"));
        loader.setController(this);
        mainLayout = (VBox) loader.load();
    }

    private void createButtons() {
        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        // defines what happens when user confirms/presses enter
        setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                // if there is a highlighted label, toggle that label first
                if (uiLogic.hasHighlightedLabel()) uiLogic.toggleSelectedLabel(
                        queryField.getText());
                // if user confirms selection, return list of labels
                return uiLogic.getResultList().entrySet().stream()
                        .filter(Map.Entry::getValue)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());
            }
            return null;
        });
    }

    private Tooltip createTitleTooltip(TurboIssue issue) {
        Tooltip titleTooltip = new Tooltip(
                (issue.isPullRequest() ? "PR #" : "Issue #") + issue.getId() + ": " + issue.getTitle());
        titleTooltip.setWrapText(true);
        titleTooltip.setMaxWidth(500);
        return titleTooltip;
    }

}
