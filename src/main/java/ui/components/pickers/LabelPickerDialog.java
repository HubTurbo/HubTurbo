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
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LabelPickerDialog extends Dialog<List<String>> {

    private static final int VBOX_SPACING = 105; // seems like some magic number
    private static final int ELEMENT_MAX_WIDTH = 400;

    private final LabelPickerUILogic uiLogic;
    private TextField textField;
    private FlowPane topPane;
    private VBox bottomBox;

    LabelPickerDialog(TurboIssue issue, List<TurboLabel> repoLabels, Stage stage) {
        // UI creation
        initialiseDialog(stage, issue);
        createButtons();
        VBox vBox = createVBox();
        Label titleLabel = createTitleLabel(issue);
        titleLabel.setTooltip(createTitleTooltip(issue));
        topPane = createTopPane();
        textField = createTextField();
        bottomBox = createBottomBox();

        setupKeyEvents();
        uiLogic = new LabelPickerUILogic(issue, repoLabels, this);

        vBox.getChildren().addAll(titleLabel, topPane, textField, bottomBox);
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

    private void ______POPULATION______() {}

    protected void populatePanes(
            List<PickerLabel> topLabels, List<PickerLabel> bottomLabels, Map<String, Boolean> groups) {
        populateTopPane(topLabels);
        populateBottomBox(bottomLabels, groups);
    }

    private void populateTopPane(List<PickerLabel> topLabels) {
        topPane.getChildren().clear();
        if (topLabels.size() == 0) {
            Label label = new Label("No currently selected labels. ");
            label.setPadding(new Insets(2, 5, 2, 5));
            topPane.getChildren().add(label);
        } else {
            topLabels.forEach(label -> topPane.getChildren().add(label.getNode()));
        }
    }

    private void populateBottomBox(List<PickerLabel> bottomLabels, Map<String, Boolean> groups) {
        bottomBox.getChildren().clear();
        if (bottomLabels.size() == 0) {
            Label label = new Label("No labels in repository. ");
            label.setPadding(new Insets(2, 5, 2, 5));
            bottomBox.getChildren().add(label);
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
                bottomBox.getChildren().addAll(groupName, groupPane);
            });

            FlowPane noGroup = new FlowPane();
            noGroup.setHgap(5);
            noGroup.setVgap(5);
            noGroup.setPadding(new Insets(5, 0, 0, 0));
            bottomLabels
                    .stream()
                    .filter(label -> !label.getGroup().isPresent())
                    .forEach(label -> noGroup.getChildren().add(label.getNode()));
            if (noGroup.getChildren().size() > 0) bottomBox.getChildren().add(noGroup);
        }
    }

    private void ______UI_CREATION______() {}

    private void initialiseDialog(Stage stage, TurboIssue issue) {
        initOwner(stage);
        initModality(Modality.APPLICATION_MODAL); // TODO change to NONE for multiple dialogs
        setTitle("Edit Labels for " + (issue.isPullRequest() ? "PR #" : "Issue #") +
                issue.getId() + " in " + issue.getRepoId());
        positionDialog(stage);
    }

    private void positionDialog(Stage stage) {
        double totalScreenWidth = Screen.getScreens().stream()
                .mapToDouble(screen -> {
                    return screen.getVisualBounds().getWidth();
                }).sum();
        double maxX = Math.max(0, totalScreenWidth - stage.getMinWidth());

        setX(Math.min(maxX, stage.getX() + stage.getScene().getWidth()));
        setY(stage.getY() + stage.getScene().getY());
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

    private VBox createBottomBox() {
        VBox bottomBox = new VBox();
        bottomBox.setPadding(new Insets(10, 0, 0, 0));
        return bottomBox;
    }

}
