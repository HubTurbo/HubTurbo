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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import ui.UI;

public class LabelPickerDialog extends Dialog<List<String>> {

    private final LabelPickerUILogic uiLogic;
    private final List<TurboLabel> repoLabels;
    private final TurboIssue issue;
    
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
        uiLogic = new LabelPickerUILogic();

        initUI(stage, issue);
        setupEvents(stage);
        Platform.runLater(queryField::requestFocus);
    }

    // Initilisation of UI

    private void initUI(Stage stage, TurboIssue issue) {
        initialiseDialog(stage, issue);
        setDialogPaneContent(issue);
        title.setTooltip(createTitleTooltip(issue));
        createButtons();
        
        List<String> finalLabels = new ArrayList<>();
        finalLabels.add("bug");
        List<String> matchedLabels = new ArrayList<>();
        populatePanes(finalLabels, matchedLabels, Optional.of("taskrunner"));
    }

    private void initialiseDialog(Stage stage, TurboIssue issue) {
        initOwner(stage);
        initModality(Modality.APPLICATION_MODAL); 
        setTitle("Edit Labels for " + (issue.isPullRequest() ? "PR #" : "Issue #") +
                issue.getId() + " in " + issue.getRepoId());
    }

    private void setDialogPaneContent(TurboIssue issue) {
        try {
            createMainLayout();
            setTitleLabel(issue);
            getDialogPane().setContent(mainLayout);
        } catch (IOException e) {
            // TODO use a HTLogger instead when failed to load fxml
            e.printStackTrace();
        }
    }

    // Population of UI elements

    /**
     * Updates ui elements based on current state
     * @param state
     */
    public void populatePanes(List<String> finalLabels, 
                              List<String> matchedLabels, 
                              Optional<String> suggestion) {
        // Population of UI elements
        populateAssignedLabels(finalLabels, suggestion);
    }

    private void populateAssignedLabels(List<String> finalLabels, Optional<String> suggestion) {
        assignedLabels.getChildren().clear();
        List<String> initialLabels = getInitialLabels();
        populateInitialLabels(initialLabels, finalLabels, suggestion);
        populateAddedLabels(getAddedLabels(initialLabels, finalLabels, suggestion), 
                            finalLabels, suggestion);
    }

    private void populateInitialLabels(List<String> initialLabels, List<String> finalLabels,
                                       Optional<String> suggestion) {
        repoLabels.stream()
            .filter(label -> initialLabels.contains(label.getActualName()))
            .map(label -> new PickerLabel(label, true))
            .forEach(label -> assignedLabels.getChildren()
                .add(label.processAssignedLabel(finalLabels, suggestion)));
    }

    private void populateAddedLabels(List<String> addedLabels, List<String> finalLabels, 
                                     Optional<String> suggestion) {
        if (!addedLabels.isEmpty()) {
            assignedLabels.getChildren().add(new Label("|"));
            repoLabels.stream()
                .filter(label -> addedLabels.contains(label.getActualName()))
                .map(label -> new PickerLabel(label, true))
                .forEach(label -> assignedLabels.getChildren()
                    .add(label.processAssignedLabel(finalLabels, suggestion)));
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
                return getFinalLabels();
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


    // Event handling 

    private void setupEvents(Stage stage) {
        setupKeyEvents();

        showingProperty().addListener(e -> {
            positionDialog(stage);
        });
    }

    private void setupKeyEvents() {
        queryField.textProperty().addListener((observable, oldValue, newValue) -> {
        });
    }

    private void positionDialog(Stage stage) {
        if (!Double.isNaN(getHeight())) {
            setX(stage.getX() + stage.getScene().getX());
            setY(stage.getY() +
                 stage.getScene().getY() +
                 (stage.getScene().getHeight() - getHeight()) / 2);
        }
    }



    // Obtain domain objects

    private List<String> getInitialLabels() {
        return issue.getLabels();
    }

    private List<String> getAddedLabels(List<String> initialLabels, List<String> finalLabels,
                                        Optional<String> suggestion) {
        List<String> addedLabels = finalLabels.stream()
            .filter(label -> !initialLabels.contains(label))
            .collect(Collectors.toList());

        if (suggestion.isPresent() 
            && !initialLabels.contains(suggestion.get()) 
            && !addedLabels.contains(suggestion.get())) {
            addedLabels.add(suggestion.get());
        }
        return addedLabels;
    }


    private List<String> getFinalLabels() {
        return repoLabels.stream()
            .map(TurboLabel::getActualName)
            .collect(Collectors.toList());
    }


    private List<String> getGroupNames(Map<String, Boolean> groups) {
        List<String> groupNames = groups.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
        Collections.sort(groupNames);
        return groupNames;
    }

}
