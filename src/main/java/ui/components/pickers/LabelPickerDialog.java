package ui.components.pickers;

import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LabelPickerDialog extends Dialog<List<String>> {

    private static final int VBOX_SPACING = 105; // seems like some magic number
    private static final int ELEMENT_MAX_WIDTH = 400;

    private final LabelPickerUILogic uiLogic;
    private TextField textField;
    private FlowPane topPane;
    private FlowPane bottomPane;

    LabelPickerDialog(TurboIssue issue, List<TurboLabel> repoLabels, Stage stage) {
        // UI creation
        initialiseDialog(stage, issue);
        createButtons();
        VBox vBox = createVBox();
        Label titleLabel = createTitleLabel(issue);
        titleLabel.setTooltip(createTitleTooltip(issue));
        topPane = createTopPane();
        textField = createTextField();
        bottomPane = createBottomPane();

        setupKeyEvents();
        uiLogic = new LabelPickerUILogic(issue, repoLabels, this);

        vBox.getChildren().addAll(titleLabel, topPane, textField, bottomPane);
        getDialogPane().setContent(vBox);

        Platform.runLater(textField::requestFocus);
    }

    private void setupKeyEvents() {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            uiLogic.processTextFieldChange(newValue.toLowerCase());
        });
        textField.setOnKeyPressed(e -> {
            if (!e.isAltDown() && !e.isMetaDown() && !e.isControlDown()) {
                if (e.getCode() == KeyCode.DOWN) {
                    e.consume();
                    uiLogic.moveHighlightOnLabel(true);
                } else if (e.getCode() == KeyCode.UP) {
                    e.consume();
                    uiLogic.moveHighlightOnLabel(false);
                } else if (e.getCode() == KeyCode.SPACE) {
                    e.consume();
                    uiLogic.toggleSelectedLabel(textField.getText());
                }
            }
        });
    }

    private void ______PANE_POPULATION______() {}

    protected void populatePanes(List<PickerLabel> topLabels, List<PickerLabel> bottomLabels) {
        populateTopPane(topLabels);
        populateBottomPane(bottomLabels);
    }

    private void populateTopPane(List<PickerLabel> topLabels) {
        topPane.getChildren().clear();
        topLabels.forEach(label -> topPane.getChildren().add(label.getNode()));
        if (topPane.getChildren().size() == 0) {
            Label label = new Label("No currently selected labels. ");
            label.setPadding(new Insets(2, 5, 2, 5));
            topPane.getChildren().add(label);
        }
    }

    private void populateBottomPane(List<PickerLabel> bottomLabels) {
        bottomPane.getChildren().clear();
        bottomLabels.forEach(label -> bottomPane.getChildren().add(label.getNode()));
        if (bottomPane.getChildren().size() == 0) {
            Label label = new Label("No labels in repository. ");
            label.setPadding(new Insets(2, 5, 2, 5));
            bottomPane.getChildren().add(label);
        }
    }

    private void ______UI_CREATION______() {}

    private void initialiseDialog(Stage stage, TurboIssue issue) {
        initOwner(stage);
        initModality(Modality.APPLICATION_MODAL); // TODO change to NONE for multiple dialogs
        setTitle("Edit Labels for " + (issue.isPullRequest() ? "PR #" : "Issue #") +
                issue.getId() + " in " + issue.getRepoId());
    }

    private void createButtons() {
        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        // defines what happens when user confirms/presses enter
        setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                // if there is a highlighted label, toggle that label first
                if (uiLogic.hasHighlightedLabel()) uiLogic.toggleSelectedLabel(textField.getText());
                // if user confirms selection, return list of labels
                return uiLogic.getResultList().entrySet().stream()
                        .filter(Map.Entry::getValue)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());
            }
            return null;
        });
    }

    private VBox createVBox() {
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(10));
        vBox.setPrefHeight(1);
        vBox.heightProperty().addListener((observable, oldValue, newValue) -> {
            setHeight(newValue.intValue() + VBOX_SPACING); // dialog box should auto-resize
        });
        return vBox;
    }

    private Label createTitleLabel(TurboIssue issue) {
        Label titleLabel = new Label(
                (issue.isPullRequest() ? "PR #" : "Issue #") + issue.getId() + ": " + issue.getTitle());
        titleLabel.setMaxWidth(ELEMENT_MAX_WIDTH);
        titleLabel.setStyle("-fx-font-size: 125%");
        return titleLabel;
    }

    private Tooltip createTitleTooltip(TurboIssue issue) {
        Tooltip titleTooltip = new Tooltip(
                (issue.isPullRequest() ? "PR #" : "Issue #") + issue.getId() + ": " + issue.getTitle());
        titleTooltip.setWrapText(true);
        titleTooltip.setMaxWidth(500);
        return titleTooltip;
    }

    private FlowPane createTopPane() {
        FlowPane topPane = new FlowPane();
        topPane.setPadding(new Insets(20, 0, 10, 0));
        topPane.setHgap(5);
        topPane.setVgap(5);
        return topPane;
    }

    private TextField createTextField() {
        TextField textField = new TextField();
        textField.setId("labelPickerTextField");
        textField.setPrefColumnCount(30);
        return textField;
    }

    private FlowPane createBottomPane() {
        FlowPane bottomPane = new FlowPane();
        bottomPane.setPadding(new Insets(10, 0, 0, 0));
        bottomPane.setHgap(5);
        bottomPane.setVgap(5);
        return bottomPane;
    }

}
