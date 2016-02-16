package ui.components.pickers;

import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;

import ui.UI;
import util.HTLog;

/**
 * Serves as a presenter that synchronizes changes in labels with dialog view  
 */
public class LabelPickerDialog extends Dialog<List<String>> implements Initializable {

    private static final int ELEMENT_MAX_WIDTH = 400;
    private static final Insets GROUPLESS_PAD = new Insets(5, 0, 0, 0);
    private static final Insets GROUP_PAD = new Insets(0, 0, 10, 10);
    private static final Logger logger = HTLog.get(LabelPickerDialog.class);

    private final List<TurboLabel> repoLabels;
    private final TurboIssue issue;
    private LabelPickerState state;

    private ChangeListener<String> listener;

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
        this.repoLabels = repoLabels;
        this.issue = issue;
        this.state = getCleanState(issue.getLabels(), getRepoLabelsSet());

        initUI(stage, issue);
        setupEvents(stage);
        Platform.runLater(queryField::requestFocus);
    }

    private LabelPickerState getCleanState(List<String> initialLabels, Set<String> repoLabels) {
        LabelPickerState state = new LabelPickerState(new HashSet<>(initialLabels));
        state = state.updateMatchedLabels(repoLabels, "");
        return state;
    }

    // Initialisation of UI

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initialiseAndregisterQueryHandler();
    }

    private void initUI(Stage stage, TurboIssue issue) {
        initialiseDialog(stage, issue);
        setDialogPaneContent(issue);
        title.setTooltip(createTitleTooltip(issue));
        createButtons();

        populatePanes(getCleanState(issue.getLabels(), getRepoLabelsSet()));
    }

    private void initialiseDialog(Stage stage, TurboIssue issue) {
        initOwner(stage);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Edit Labels for " + (issue.isPullRequest() ? "PR #" : "Issue #") +
                issue.getId() + " in " + issue.getRepoId());
    }

    private void setDialogPaneContent(TurboIssue issue) {
        createMainLayout();
        setTitleLabel(issue);
        getDialogPane().setContent(mainLayout);
    }

    // Population of UI elements

    /**
     * Populate respective panes with labels that matches current user input
     * @param state
     */
    private void populatePanes(LabelPickerState state) {
        // Population of UI elements
        populateAssignedLabels(state.getInitialLabels(), state.getRemovedLabels(), state.getAddedLabels(),
                state.getCurrentSuggestion());
        populateFeedbackLabels(state.getAssignedLabels(), state.getMatchedLabels(), state.getCurrentSuggestion());
        // Ensures dialog pane resize according to content
        getDialogPane().getScene().getWindow().sizeToScene();
    }

    private void populateAssignedLabels(List<String> initialLabels, List<String> removedLabels,
                                        List<String> addedLabels, Optional<String> suggestion) {
        assignedLabels.getChildren().clear();
        populateInitialLabels(initialLabels, removedLabels, suggestion);
        populateToBeAddedLabels(addedLabels, suggestion);
        if (initialLabels.isEmpty()) createTextLabel("No currently selected labels. ");
    }

    private void populateInitialLabels(List<String> initialLabels, List<String> removedLabels,
                                       Optional<String> suggestion) {
        initialLabels.stream()
            .forEach(label -> assignedLabels.getChildren()
            .add(processInitialLabel(label, removedLabels, suggestion)));
    }

    private Node processInitialLabel(String initialLabel, List<String> removedLabels, 
                                            Optional<String> suggestion) {
        TurboLabel repoInitialLabel = getRepoTurboLabel(initialLabel);
        if (!removedLabels.contains(initialLabel)) {
            if (suggestion.isPresent() && initialLabel.equals(suggestion.get())) {
                return getStyledPickerLabel(repoInitialLabel, true, false, true, false, true);
            }
            return getStyledPickerLabel(repoInitialLabel, false, false, false, false, true);
        }

        if (suggestion.isPresent() && initialLabel.equals(suggestion.get())) {
            return getStyledPickerLabel(repoInitialLabel, true, false, false, false, true);
        }

        return getStyledPickerLabel(repoInitialLabel, false, false, true, false, true);
    }

    private Node getStyledPickerLabel(TurboLabel label, 
        boolean isFaded, boolean isHighlighted, boolean isRemoved, boolean isSelected, boolean isTop) {
        PickerLabel styledLabel = new PickerLabel(label, isTop);
        styledLabel.setIsFaded(isFaded);
        styledLabel.setIsHighlighted(isHighlighted);
        styledLabel.setIsRemoved(isRemoved);
        styledLabel.setIsSelected(isSelected);

        Node node = styledLabel.getNode();
        node.setOnMouseClicked(e -> handleLabelClick(styledLabel));
        return node;
    }

    /**
     * To return the repo's TurboLabel that matches labelName
     * Assumption: the labelName matches exactly 1 TurboLabel
     *
     * @param labelName
     * @return
     */
    private TurboLabel getRepoTurboLabel(String labelName) {
        assert repoLabels.stream()
                .filter(label -> label.getActualName().equals(labelName))
                .findFirst()
                .isPresent();
        return repoLabels.stream()
                .filter(label -> label.getActualName().equals(labelName))
                .findFirst()
                .get();
    }

    private void populateToBeAddedLabels(List<String> addedLabels, Optional<String> suggestion) {
        if (!addedLabels.isEmpty() || hasNewSuggestion(addedLabels, suggestion)) {
            assignedLabels.getChildren().add(new Label("|"));
        }
        populateAddedLabels(addedLabels, suggestion);
        populateSuggestedLabel(addedLabels, suggestion);
    }

    private void populateAddedLabels(List<String> addedLabels, Optional<String> suggestion) {
        addedLabels.stream()
                .forEach(label -> {
                    assignedLabels.getChildren().add(processAddedLabel(label, suggestion));
                });
    }

    private Node processAddedLabel(String addedLabel, Optional<String> suggestion) {
        if (!suggestion.isPresent() || !addedLabel.equals(suggestion.get())) {
            return getStyledPickerLabel(getRepoTurboLabel(addedLabel), false, false, false, false, true);
        }
        return getStyledPickerLabel(getRepoTurboLabel(addedLabel), true, false, true, false, true);
    }

    private void populateSuggestedLabel(List<String> addedLabels, Optional<String> suggestion) {
        if (hasNewSuggestion(addedLabels, suggestion)) {
            assignedLabels.getChildren().add(processSuggestedLabel(suggestion.get()));
        }
    }

    private boolean hasNewSuggestion(List<String> addedLabels, Optional<String> suggestion) {
        return suggestion.isPresent() 
            && !(issue.getLabels()).contains(suggestion.get()) && !addedLabels.contains(suggestion.get());
    }

    private Node processSuggestedLabel(String suggestedLabel) {
        return getStyledPickerLabel(getRepoTurboLabel(suggestedLabel), true, false, false, false, true);
    }

    private void populateFeedbackLabels(List<String> assignedLabels, List<String> matchedLabels,
                                        Optional<String> suggestion) {
        feedbackLabels.getChildren().clear();
        populateGroupLabels(assignedLabels, matchedLabels, suggestion);
        populateGrouplessLabels(assignedLabels, matchedLabels, suggestion);
    }

    private void populateGroupLabels(List<String> finalLabels, List<String> matchedLabels,
                                     Optional<String> suggestion) {

        Map<String, FlowPane> groupContent = getGroupContent(finalLabels, matchedLabels, suggestion);
        groupContent.entrySet().stream().forEach(entry -> {
            feedbackLabels.getChildren().addAll(
                    createGroupTitle(entry.getKey()), entry.getValue());
        });
    }

    private Map<String, FlowPane> getGroupContent(List<String> finalLabels, List<String> matchedLabels,
                                                  Optional<String> suggestion) {
        Map<String, FlowPane> groupContent = new HashMap<>();
        repoLabels.stream().sorted()
                .filter(label -> label.getGroup().isPresent())
                .map(label -> new PickerLabel(label, false))
                .forEach(label -> {
                    String group = label.getGroupName().get();
                    if (!groupContent.containsKey(group)) {
                        groupContent.put(group, createGroupPane(GROUP_PAD));
                    }
                    groupContent.get(group).getChildren().add(processMatchedLabel(
                        label.getActualName(), matchedLabels, finalLabels, suggestion));
                });
        return groupContent;
    }

    private void populateGrouplessLabels(List<String> finalLabels, List<String> matchedLabels,
                                         Optional<String> suggestion) {
        FlowPane groupless = createGroupPane(GROUPLESS_PAD);
        repoLabels.stream()
                .filter(label -> !label.getGroup().isPresent())
                .map(label -> new PickerLabel(label, false))
                .forEach(label -> groupless.getChildren().add(processMatchedLabel(
                    label.getActualName(), matchedLabels, finalLabels, suggestion)));

        if (!groupless.getChildren().isEmpty()) feedbackLabels.getChildren().add(groupless);
    }

    private Node processMatchedLabel(String repoLabel, List<String> matchedLabels, 
                                            List<String> assignedLabels, Optional<String> suggestion) {
        boolean shouldHighlight = suggestion.isPresent() && suggestion.get().equals(repoLabel);
        return getStyledPickerLabel(getRepoTurboLabel(repoLabel), !matchedLabels.contains(repoLabel), 
                                    shouldHighlight, false, assignedLabels.contains(repoLabel), false);
    }

    private void createMainLayout() {
        FXMLLoader loader = new FXMLLoader(UI.class.getResource("fxml/LabelPickerView.fxml"));
        loader.setController(this);
        try {
            mainLayout = (VBox) loader.load();
        } catch (IOException e) {
            logger.error("Failure to load FXML. " + e.getMessage());
            close();
        }
    }

    private void createButtons() {
        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        // defines what happens when user confirms/presses enter
        setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return state.getAssignedLabels();
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

    private void setTitleLabel(TurboIssue issue) {
        title.setText((issue.isPullRequest() ? "PR #" : "Issue #")
                + issue.getId() + ": " + issue.getTitle());
    }

    private Label createTextLabel(String input) {
        Label label = new Label(input);
        label.setPadding(new Insets(2, 5, 2, 5));
        return label;
    }


    private Label createGroupTitle(String name) {
        Label groupName = new Label(name);
        groupName.setPadding(new Insets(0, 5, 5, 0));
        groupName.setMaxWidth(ELEMENT_MAX_WIDTH - 10);
        groupName.setStyle("-fx-font-size: 110%; -fx-font-weight: bold;");
        return groupName;
    }

    private FlowPane createGroupPane(Insets padding) {
        FlowPane group = new FlowPane();
        group.setHgap(5);
        group.setVgap(5);
        group.setPadding(padding);
        return group;
    }

    // Event handling 

    private void setupEvents(Stage stage) {
        showingProperty().addListener(e -> {
            positionDialog(stage);
        });
    }

    private void initialiseAndregisterQueryHandler() {
        listener = (observable, oldValue, newValue) -> {
            state = LabelPickerState.determineState(
                    getCleanState(issue.getLabels(), getRepoLabelsSet()),
                    getRepoLabelsSet(), queryField.getText().toLowerCase());
            populatePanes(state);
        };
        queryField.textProperty().addListener(listener);
    }

    private void handleLabelClick(PickerLabel label) {
        state.toggleLabel(getRepoLabelsSet(), label.getActualName());
        populatePanes(state);

        if (!queryField.isDisabled()) {
            queryField.setDisable(true);
        }
        queryField.textProperty().removeListener(listener);
        queryField.clear();
    }

    private void positionDialog(Stage stage) {
        if (getDialogHeight().isPresent()) {
            setX(stage.getX() + stage.getScene().getX());
            setY(stage.getY() +
                    stage.getScene().getY() +
                    (stage.getScene().getHeight() - getHeight()) / 2);
        }
    }

    // getHeight() returns NaN when the height is not set
    private Optional<Double> getDialogHeight() {
        return Double.isNaN(getHeight()) ? Optional.empty() : Optional.of(getHeight());
    }

    private Set<String> getRepoLabelsSet() {
        return repoLabels.stream()
                .map(TurboLabel::getActualName)
                .collect(Collectors.toSet());
    }

}
