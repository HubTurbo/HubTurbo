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

import java.util.*;
import java.util.stream.Collectors;

public class LabelPickerDialog extends Dialog<List<String>> {

    public static final int VBOX_SPACING = 105; // seems like some magic number

    private TurboIssue issue;
    private TextField textField;
    private List<TurboLabel> allLabels;
    private List<PickerLabel> topLabels = new ArrayList<>();
    private List<PickerLabel> bottomLabels;
    private Map<String, Boolean> resultList = new HashMap<>();
    private FlowPane topPane;
    private FlowPane bottomPane;
    private Optional<String> possibleAddition = Optional.empty();

    LabelPickerDialog(TurboIssue issue, List<TurboLabel> allLabels, Stage stage) {
        initOwner(stage);
        initModality(Modality.APPLICATION_MODAL); // TODO change to NONE for multiple dialogs
        setTitle("Edit Labels for " + (issue.isPullRequest() ? "PR #" : "Issue #") +
                issue.getId() + " in " + issue.getRepoId());
        setHeaderText((issue.isPullRequest() ? "PR #" : "Issue #") + issue.getId() + ": " + issue.getTitle());

        this.issue = issue;
        this.allLabels = allLabels;
        // populate resultList by going through allLabels and seeing which ones currently exist
        // in issue.getLabels()
        allLabels.forEach(label ->
                resultList.put(label.getActualName(), issue.getLabels().contains(label.getActualName())));

        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        VBox vBox = new VBox();
        vBox.setPadding(new Insets(10));
        vBox.setPrefHeight(1);

        topPane = new FlowPane();
        topPane.setPadding(new Insets(0, 0, 10, 0));
        topPane.setHgap(5);
        topPane.setVgap(5);

        textField = new TextField();
        textField.setPrefColumnCount(30);
        setupKeyEvents();

        bottomPane = new FlowPane();
        bottomPane.setPadding(new Insets(10, 0, 0, 0));
        bottomPane.setHgap(5);
        bottomPane.setVgap(5);

        addExistingLabels();
        updateBottomLabels("");
        populatePanes();

        vBox.getChildren().addAll(topPane, textField, bottomPane);
        getDialogPane().setContent(vBox);
        vBox.heightProperty().addListener((observable, oldValue, newValue) -> {
            setHeight(newValue.intValue() + VBOX_SPACING); // dialog box should auto-resize
        });

        // defines what happens when user confirms/presses enter
        setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                // if there is a highlighted label, toggle that label first
                if (hasHighlightedLabel()) {
                    toggleSelectedLabel();
                }
                // if user confirms selection, return list of labels
                return resultList.entrySet().stream()
                        .filter(Map.Entry::getValue)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());
            }
            return null;
        });
        requestFocus();
    }

    protected void requestFocus() {
        Platform.runLater(textField::requestFocus);
    }

    private void populatePanes() {
        populateTopPane();
        populateBottomPane();
    }

    private void populateTopPane() {
        topPane.getChildren().clear();
        topLabels.forEach(label -> topPane.getChildren().add(label.getNode()));
        if (topPane.getChildren().size() == 0) {
            Label label = new Label("No currently selected labels. ");
            label.setPadding(new Insets(2, 5, 2, 5));
            topPane.getChildren().add(label);
        }
    }

    private void populateBottomPane() {
        bottomPane.getChildren().clear();
        bottomLabels.forEach(label -> bottomPane.getChildren().add(label.getNode()));
        if (bottomPane.getChildren().size() == 0) {
            Label label = new Label("No labels in repository. ");
            label.setPadding(new Insets(2, 5, 2, 5));
            bottomPane.getChildren().add(label);
        }
    }

    private void setupKeyEvents() {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(" ")) {
                textField.setText("");
            } else {
                updateBottomLabels(newValue.toLowerCase());
                if (hasHighlightedLabel()) {
                    addRemovePossibleLabel(getHighlightedLabelName().get().getActualName());
                } else {
                    addRemovePossibleLabel("");
                }
                populatePanes();
            }
        });
        textField.setOnKeyPressed(e -> {
            if (!e.isAltDown() && !e.isMetaDown() && !e.isControlDown()) {
                if (e.getCode() == KeyCode.DOWN) {
                    e.consume();
                    moveHighlightOnLabel(true);
                    populatePanes();
                } else if (e.getCode() == KeyCode.UP) {
                    e.consume();
                    moveHighlightOnLabel(false);
                    populatePanes();
                } else if (e.getCode() == KeyCode.SPACE) {
                    e.consume();
                    addRemovePossibleLabel("");
                    toggleSelectedLabel();
                }
            }
        });
    }

    public void toggleLabel(String name) {
        processLabelChange(name);
        textField.setText("");
        updateBottomLabels("");
        populatePanes();
    }

    private void toggleSelectedLabel() {
        if (!bottomLabels.isEmpty() && !textField.getText().isEmpty() && hasHighlightedLabel()) {
            toggleLabel(
                    bottomLabels.stream().filter(PickerLabel::isHighlighted).findFirst().get().getActualName());
        }
    }

    private void processLabelChange(String name) {
        Optional<TurboLabel> turboLabel =
                allLabels.stream().filter(label -> label.getActualName().equals(name)).findFirst();
        if (turboLabel.isPresent()) {
            if (turboLabel.get().isExclusive() && !resultList.get(name)) {
                // exclusive label check
                String group = turboLabel.get().getGroup().get();
                allLabels
                        .stream()
                        .filter(TurboLabel::isExclusive)
                        .filter(label -> label.getGroup().get().equals(group))
                        .forEach(label -> updateTopLabels(label.getActualName(), false));
                updateTopLabels(name, true);
            } else {
                updateTopLabels(name, !resultList.get(name));
            }
        }
    }

    private void addExistingLabels() {
        // used once to populate topLabels at the start
        allLabels.stream()
                .filter(label -> issue.getLabels().contains(label.getActualName()))
                .forEach(label -> topLabels.add(new PickerLabel(label, this)));
    }

    private void updateTopLabels(String name, boolean isAdd) {
        // adds new labels to the end of the list
        resultList.put(name, isAdd); // update resultList first
        if (isAdd) {
            if (issue.getLabels().contains(name)) {
                topLabels.stream()
                        .filter(label -> label.getActualName().equals(name))
                        .forEach(label -> {
                            label.setIsRemoved(false);
                            label.setIsFaded(false);
                        });
            } else {
                allLabels.stream()
                        .filter(label -> label.getActualName().equals(name))
                        .filter(label -> resultList.get(label.getActualName()))
                        .filter(label -> !isInTopLabels(label.getActualName()))
                        .findFirst()
                        .ifPresent(label -> topLabels.add(new PickerLabel(label, this)));
            }
        } else {
            if (issue.getLabels().contains(name)) {
                topLabels.stream()
                        .filter(label -> label.getActualName().equals(name))
                        .findFirst()
                        .ifPresent(label -> label.setIsRemoved(true));
            } else {
                topLabels.stream()
                        .filter(label -> label.getActualName().equals(name))
                        .findFirst()
                        .ifPresent(topLabels::remove);
            }
        }
    }

    private boolean isInTopLabels(String name) {
        // used to prevent duplicates in topLabels
        return topLabels.stream()
                .filter(label -> label.getActualName().equals(name))
                .findAny()
                .isPresent();
    }

    private void updateBottomLabels(String match) {
        // get all labels that contain search query
        // fade out labels which do not match
        bottomLabels = allLabels
                .stream()
                .map(label -> new PickerLabel(label, this))
                .map(label -> {
                    if (resultList.get(label.getActualName())) {
                        label.setIsSelected(true); // add tick if selected
                    }
                    if (!match.isEmpty() && !containsIgnoreCase(label.getActualName(), match)) {
                        label.setIsFaded(true); // fade out if does not match search query
                    }
                    return label;
                })
                .collect(Collectors.toList());

        if (!match.isEmpty() && !bottomLabels.isEmpty()) {
            // highlight the first matching item
            bottomLabels.stream()
                    .filter(label -> containsIgnoreCase(label.getActualName(), match))
                    .findFirst()
                    .ifPresent(label -> {
                        label.setIsHighlighted(true);
                    });
        }
    }

    private boolean hasHighlightedLabel() {
        return bottomLabels.stream()
                .filter(PickerLabel::isHighlighted)
                .findAny()
                .isPresent();
    }

    private Optional<PickerLabel> getHighlightedLabelName() {
        return bottomLabels.stream()
                .filter(PickerLabel::isHighlighted)
                .findAny();
    }

    private boolean containsIgnoreCase(String source, String query) {
        return source.toLowerCase().contains(query.toLowerCase());
    }

    private void moveHighlightOnLabel(boolean isDown) {
        if (hasHighlightedLabel()) {
            // used to move the highlight on the bottom labels
            // find all matching labels
            List<PickerLabel> matchingLabels = bottomLabels.stream()
                    .filter(label -> !label.isFaded())
                    .collect(Collectors.toList());

            // move highlight around
            for (int i = 0; i < matchingLabels.size(); i++) {
                if (matchingLabels.get(i).isHighlighted()) {
                    if (isDown && i < matchingLabels.size() - 1) {
                        matchingLabels.get(i).setIsHighlighted(false);
                        matchingLabels.get(i + 1).setIsHighlighted(true);
                    } else if (!isDown && i > 0) {
                        matchingLabels.get(i - 1).setIsHighlighted(true);
                        matchingLabels.get(i).setIsHighlighted(false);
                    }
                    return;
                }
            }
        }
    }

    private void addRemovePossibleLabel(String name) {
        // deal with previous selection
        if (possibleAddition.isPresent()) {
            // if there's a previous possible selection
            topLabels.stream()
                    .filter(label -> label.getActualName().equals(possibleAddition.get()))
                    .findFirst()
                    .ifPresent(label -> {
                        if (issue.getLabels().contains(possibleAddition.get()) ||
                                resultList.get(possibleAddition.get())) {
                            // if it is an existing label toggle fade and strike through
                            label.setIsFaded(false);
                            if (resultList.get(label.getActualName())) {
                                label.setIsRemoved(false);
                            } else {
                                label.setIsRemoved(true);
                            }
                        } else {
                            // if not then remove it
                            topLabels.remove(label);
                        }
                    });
            possibleAddition = Optional.empty();
        }

        if (!name.isEmpty()) {
            // deal with current selection
            if (isInTopLabels(name)) {
                // if it exists in the top pane
                topLabels.stream()
                        .filter(label -> label.getActualName().equals(name))
                        .findFirst()
                        .ifPresent(label -> {
                            if (issue.getLabels().contains(name)) {
                                // if it is an existing label toggle fade and strike through
                                label.setIsFaded(resultList.get(name));
                                label.setIsRemoved(resultList.get(name));
                            } else {
                                // else set fade and strike through
                                label.setIsFaded(true);
                                label.setIsRemoved(true);
                            }
                        });
            } else {
                // add it to the top pane
                allLabels.stream()
                        .filter(label -> label.getActualName().equals(name))
                        .findFirst()
                        .ifPresent(label -> topLabels.add(new PickerLabel(label, this, false, false, false, true)));
            }
            possibleAddition = Optional.of(name);
        }
    }

}
