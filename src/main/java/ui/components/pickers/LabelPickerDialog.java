package ui.components.pickers;

import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
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
import util.Utility;

/**
 * Serves as a presenter that synchronizes changes in labels with dialog view  
 * @author jinified
 *
 */
public class LabelPickerDialog extends Dialog<List<String>> implements Initializable {

    private static final int ELEMENT_MAX_WIDTH = 400;
    private static final Insets GROUPLESS_PAD = new Insets(5, 0, 0, 0);
    private static final Insets GROUP_PAD = new Insets(0, 0, 10, 10);
    private static final Logger logger = HTLog.get(LabelPickerDialog.class);

    private final LabelPickerUILogic uiLogic;
    private final List<TurboLabel> repoLabels;
    private final List<String> initialLabels;
    private final Set<String> repoLabelsSet;
    private final TurboIssue issue;

    private List<String> finalAssignedLabels;
    
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
        repoLabelsSet = getRepoLabelsSet();
        initialLabels = issue.getLabels();
        finalAssignedLabels = new ArrayList<>(initialLabels);
        uiLogic = new LabelPickerUILogic();

        initUI(stage, issue);
        setupEvents(stage);
        Platform.runLater(queryField::requestFocus);
    }

    void handleLabelClick(PickerLabel label) {
        if (!queryField.isDisabled()) {
            queryField.setDisable(true);
        }
        finalAssignedLabels = updateFinalAssignedLabels(label);
        populatePanes(new ArrayList<>(), Optional.empty());
    }

    // Initilisation of UI

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        registerQueryHandler();
    }

    private void initUI(Stage stage, TurboIssue issue) {
        initialiseDialog(stage, issue);
        setDialogPaneContent(issue);
        title.setTooltip(createTitleTooltip(issue));
        createButtons();
        
        populatePanes(new ArrayList<>(), Optional.empty());
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
     * Updates ui elements based on current state
     * @param state
     */
    public void populatePanes(List<String> matchedLabels, Optional<String> suggestion) {
        // Population of UI elements
        populateAssignedLabels(finalAssignedLabels, suggestion);
        populateFeedbackLabels(finalAssignedLabels, matchedLabels, suggestion);
    }

    private void populateAssignedLabels(List<String> finalLabels, Optional<String> suggestion) {
        assignedLabels.getChildren().clear();
        populateInitialLabels(initialLabels, finalLabels, suggestion);
        populateAddedLabels(getAddedLabels(initialLabels, finalLabels, suggestion), 
                            finalLabels, suggestion);

        if (finalAssignedLabels.isEmpty()) createTextLabel("No currently selected labels. ");
    }

    private void populateInitialLabels(List<String> initialLabels, List<String> finalLabels,
                                       Optional<String> suggestion) {
        repoLabels.stream()
            .filter(label -> initialLabels.contains(label.getActualName()))
            .map(label -> new PickerLabel(this, label, true))
            .forEach(label -> assignedLabels.getChildren()
                .add(label.processAssignedLabel(finalLabels, suggestion)));
    }

    private void populateAddedLabels(List<String> addedLabels, List<String> finalLabels, 
                                     Optional<String> suggestion) {
        if (!addedLabels.isEmpty()) {
            assignedLabels.getChildren().add(new Label("|"));
            repoLabels.stream()
                .filter(label -> addedLabels.contains(label.getActualName()))
                .map(label -> new PickerLabel(this, label, true))
                .forEach(label -> assignedLabels.getChildren()
                    .add(label.processAssignedLabel(finalLabels, suggestion)));
        }
    }

    private void populateFeedbackLabels(List<String> finalLabels, List<String> matchedLabels,
                                        Optional<String> suggestion) {
        feedbackLabels.getChildren().clear();
        populateGroupLabels(finalLabels, matchedLabels, suggestion);
        populateGrouplessLabels(finalLabels, matchedLabels, suggestion);
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
            .map(label -> new PickerLabel(this, label, false))
            .forEach(label -> {
                String group = label.getGroupName().get();
                if (!groupContent.containsKey(group)) {
                    groupContent.put(group, createGroupPane(GROUP_PAD));
                } 
                groupContent.get(group).getChildren()
                    .add(label.processChoiceLabel(finalLabels, matchedLabels, suggestion));
            });
        return groupContent;
    }

    private void populateGrouplessLabels(List<String> finalLabels, List<String> matchedLabels, 
                                         Optional<String> suggestion) {
        FlowPane groupless = createGroupPane(GROUPLESS_PAD);
        repoLabels.stream()                                                               
            .filter(label -> !label.getGroup().isPresent())                               
            .map(label -> new PickerLabel(this, label, false))
            .forEach(label -> groupless.getChildren()
                .add(label.processChoiceLabel(finalLabels, matchedLabels, suggestion)));
        
        if (!groupless.getChildren().isEmpty()) feedbackLabels.getChildren().add(groupless);
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
                return finalAssignedLabels;
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

    private void registerQueryHandler() {
        queryField.textProperty().addListener((observable, oldValue, newValue) -> {
            LabelPickerState state = uiLogic.determineState(
                new LabelPickerState(new HashSet<String>(issue.getLabels())), 
                repoLabelsSet, queryField.getText().toLowerCase());
            finalAssignedLabels = state.getAssignedLabels();
            populatePanes(state.getMatchedLabels(), state.getCurrentSuggestion());
        });
    }

    private void positionDialog(Stage stage) {
        if (getDialogHeight().isPresent()) {
            setX(stage.getX() + stage.getScene().getX());
            setY(stage.getY() +
                 stage.getScene().getY() +
                 (stage.getScene().getHeight() - getHeight()) / 2);
        } 
    }

    // TODO refactor if else 
    private List<String> updateFinalAssignedLabels(PickerLabel selectedLabel) {
        List<String> finalLabels = finalAssignedLabels;
        String labelName = selectedLabel.getActualName();

        if (!finalLabels.contains(labelName)) {
            if (selectedLabel.isExclusive()) {
                finalLabels.removeIf(label -> label.contains(selectedLabel.getGroup().get()));
            }

            finalLabels.add(labelName);
        } else {
            finalLabels.remove(labelName);
        }
        return finalLabels;
    }

    private Optional<Double> getDialogHeight() {
        return Double.isNaN(getHeight()) ? Optional.empty() : Optional.of(getHeight());
    }

    private List<String> getAddedLabels(List<String> initialLabels, List<String> finalLabels,
                                        Optional<String> suggestion) {
        List<String> addedLabels = finalLabels.stream()
            .filter(label -> !initialLabels.contains(label))
            .collect(Collectors.toList());

        if (suggestion.isPresent() && !initialLabels.contains(suggestion.get()) 
            && !addedLabels.contains(suggestion.get())) {
            addedLabels.add(suggestion.get());
        }
        return addedLabels;
    }


    private Set<String> getRepoLabelsSet() {
        return repoLabels.stream()
            .map(TurboLabel::getActualName)
            .collect(Collectors.toSet());
    }

}
