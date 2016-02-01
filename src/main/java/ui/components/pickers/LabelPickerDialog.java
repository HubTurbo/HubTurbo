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
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import ui.UI;

public class LabelPickerDialog extends Dialog<List<String>> {

    private static final int VBOX_SPACING = 105;
    private static final int ELEMENT_MAX_WIDTH = 400;
    private static final String EXCLUSIVE_DELIMITER = ".";
    private static final String NONEXCLUSIVE_DELIMITER = "-";

    private final LabelPickerUILogic uiLogic;
    private final List<TurboLabel> repoLabels;
    private final Set<String> repoLabelsString;
    private final Map<String, Boolean> groups;
    private final TurboIssue issue;
    
    private List<String> initialLabels;
    private List<String> addedLabels;
    private List<String> removedLabels;
    private List<String> matchedLabels;
    private List<String> finalLabels;
    

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
        this.repoLabelsString = repoLabels.stream()
                .map(TurboLabel::getActualName)
                .collect(Collectors.toSet());
        this.issue = issue;
        LabelPickerState state = new LabelPickerState(new HashSet<String>(issue.getLabels()));
        uiLogic = new LabelPickerUILogic();

        // UI creation
        initUI(stage, issue);
        updateUI(state);
        setupEvents(stage);
        uiLogic = new LabelPickerUILogic(issue, repoLabels, this);
        Platform.runLater(queryField::requestFocus);
    }

    // Initilisation of UI

    private void initialiseDialog(Stage stage, TurboIssue issue) {
        initOwner(stage);
        initModality(Modality.APPLICATION_MODAL); // TODO change to NONE for multiple dialogs
        setTitle("Edit Labels for " + (issue.isPullRequest() ? "PR #" : "Issue #") +
                issue.getId() + " in " + issue.getRepoId());
    }

    private void positionDialog(Stage stage) {
        if (!Double.isNaN(getHeight())) {
            setX(stage.getX() + stage.getScene().getX());
            setY(stage.getY() +
                 stage.getScene().getY() +
                 (stage.getScene().getHeight() - getHeight()) / 2);
        }
    }


    private void initUI(Stage stage, TurboIssue issue) {
        initialiseDialog(stage, issue);
        createButtons();
        setDialogPaneContent(issue);
        title.setTooltip(createTitleTooltip(issue));
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

    // TODO wrap in scrollpane 
    private void createMainLayout() throws IOException {
        FXMLLoader loader = new FXMLLoader(UI.class.getResource("fxml/LabelPickerView.fxml"));
        loader.setController(this);
        mainLayout = (VBox) loader.load();
        // Auto-resizing
        mainLayout.heightProperty().addListener((observable, oldValue, newValue) -> {
            setHeight(newValue.intValue() + VBOX_SPACING); 
        });
    }

    private void setTitleLabel(TurboIssue issue) {
        title.setText((issue.isPullRequest() ? "PR #" : "Issue #") 
            + issue.getId() + ": " + issue.getTitle());
    }

    // TODO returns result via showAndWait
    private void createButtons() {
        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        // defines what happens when user confirms/presses enter
        setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return finalLabels;
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

    private void setupEvents(Stage stage) {
        setupKeyEvents();

        showingProperty().addListener(e -> {
            positionDialog(stage);
        });
    }

    private void setupKeyEvents() {
        queryField.textProperty().addListener((observable, oldValue, newValue) -> {
            uiLogic.determineState(new LabelPickerState(new HashSet<String>(issue.getLabels())),
                    repoLabelsString,
                    queryField.getText().toLowerCase());
        });
    }

    private void handleClick(Label label) {
        // Disable text field upon clicking on a label
        queryField.setDisable(true);
        finalLabels = processClickedLabel(label, label.getText());
        updateUIOnClick();
    }

    // Overloaded to handle clicking on label with delimiter
    private void handleClick(Label label, String fullName) {
        // Disable text field upon clicking on a label
        queryField.setDisable(true);
        finalLabels = processClickedLabel(label, fullName);
        updateUIOnClick();
    }
    // Populate UI elements with LabelPickerState

    /**
     * Updates ui elements based on current state
     * @param state
     */
    public void updateUI(LabelPickerState state) {
        initialLabels = state.getInitialLabels();
        addedLabels = state.getAddedLabels();
        removedLabels = state.getRemovedLabels();
        matchedLabels = state.getMatchedLabels();
        finalLabels = state.getAssignedLabels();
        Optional<String> suggestion = state.getCurrentSuggestion();
        
        // Population of UI elements
        populateAssignedLabels(initialLabels, addedLabels, removedLabels, suggestion);
        populateFeedbackLabels(initialLabels, finalLabels, matchedLabels, suggestion);
    }

    // Update UI on click
    public void updateUIOnClick() {
        // Population of UI elements
        populateAssignedLabels(initialLabels, addedLabels, removedLabels, Optional.empty());
        populateFeedbackLabels(initialLabels, finalLabels, matchedLabels, Optional.empty());
    }

    private boolean hasNoLabels(List<String> initialLabels, 
                                List<String> addedLabels) {
        return initialLabels.isEmpty() && addedLabels.isEmpty();
    }

    private Label createTextLabel(String input) {
        Label label = new Label(input);
        label.setPadding(new Insets(2, 5, 2, 5));
        return label;
    }

    private List<Label> populateInitialLabels(List<String> initialLabels, 
                                               List<String> removedLabels,
                                               Optional<String> suggestion) {
        return initialLabels.stream().filter(label -> !removedLabels.contains(label))
            .map(label -> createSolidLabel(label, suggestion))
            .collect(Collectors.toList());
    }


    // TODO remove check for existence of label after fixing LabelPickerState
    private Label processAddedLabel(String name, Optional<String> suggestion) {
        Label label = createBasicLabel(name);
        if (suggestion.isPresent() && suggestion.get().equals(label.getText())) {
            setFadedLabel(label);
            setStrikedLabel(label);
        }
        return label;
    }

    // TODO Given added list how to know which one is faded and strike
    private List<Label> populateAddedLabels(List<String> addedLabels, 
                                            List<String> initialLabels, 
                                            Optional<String> suggestion) {
        List<Label> nextAddedLabels =  addedLabels.stream()
            .map(label -> createSolidLabel(label, suggestion))
            .collect(Collectors.toList());

        // Add faded label to indicated suggested but not added 
        if (suggestion.isPresent() && !nextAddedLabels.contains(suggestion)) {
           nextAddedLabels.add(createFadedLabel(suggestion.get()));
            .filter(label -> repoLabelsString.contains(label))
            .map(label -> processAddedLabel(label, suggestion))
            .collect(Collectors.toList());

        // Faded and striked when already present in addedLabels
        if (suggestion.isPresent() && !addedLabels.contains(suggestion.get())
            && !initialLabels.contains(suggestion.get())) {
            Label addedLabel = createBasicLabel(suggestion.get());
            setFadedLabel(addedLabel);
            nextAddedLabels.add(addedLabel);
        }
        
        return nextAddedLabels;
    }

    private void populateTopPane(List<PickerLabel> existingLabels, List<PickerLabel> newTopLabels) {
        assignedLabels.getChildren().clear();
        List<Label> nextInitialLabels = populateInitialLabels(initialLabels, 
                removedLabels, suggestion);
        List<Label> nextAddedLabels = populateAddedLabels(addedLabels, initialLabels, 
                                                          suggestion);
        if (hasNoLabels(initialLabels, addedLabels)) {
            Label label = createTextLabel("No currently selected labels. ");
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
            List<String> groupNames = getGroupNames(groups);
            groupNames.forEach(group -> {
                addGroup(repoLabels, groups, group, matchedLabels, assignedLabels,
                         suggestion);
            });
            addNoGroup(matchedLabels, assignedLabels, suggestion);
        }
    }
                                        
    // Utility methods

    private List<String> processClickedLabel(Label label, String fullName) {
        String name = fullName.split(" ")[0];

        if (addedLabels.contains(name)) {
            addedLabels.remove(name);
            return getFinalLabels(initialLabels, addedLabels, removedLabels);
        }

        if (!initialLabels.contains(name) && !addedLabels.contains(name)) {
            addedLabels.add(name);
            return getFinalLabels(initialLabels, addedLabels, removedLabels);
        }


        if (removedLabels.contains(name)) {
            removedLabels.remove(name);
            return getFinalLabels(initialLabels, addedLabels, removedLabels);
        }

        if (initialLabels.contains(name)) {
            removedLabels.add(name);
            return getFinalLabels(initialLabels, addedLabels, removedLabels);
        }
        
        return finalLabels;
    }

    // TODO remove side effect
    private void addSelected(List<Label> labels, List<String> matchedLabels) {
        labels.stream().forEach(label -> {
            label.setText(label.getText() + " ✓" );});
    }

    private Label processGroupLabels(TurboLabel label, List<String> matchedLabels,
                                     List<String> assignedLabels, 
                                     Optional<String> suggestion) {
        Label newLabel = createGroupLabel(label.getActualName());
        if (matchedLabels.size() > 0 && !matchedLabels.contains(label.getActualName())) {
            setFadedLabel(newLabel);
        }

        if (suggestion.isPresent() && suggestion.get().equals(label.getActualName())) {
            setSuggestedLabel(newLabel);
        }

        if (assignedLabels.contains(label.getActualName())) {
            setSelected(newLabel);
        }
        return newLabel;
    }

    private Label processFeedbackLabel(Label label, List<String> matchedLabels, 
                                       List<String> assignedLabels, 
                                       Optional<String> suggestion) {
        if (matchedLabels.size() > 0 && !matchedLabels.contains(label.getText())) {
            setFadedLabel(label);
        }

        if (suggestion.isPresent() && suggestion.get().equals(label.getText())) {
            setSuggestedLabel(label);
        }
        
        if (assignedLabels.contains(label.getText())) {
            setSelected(label);
        }
        return label;
    }

    private void addGroup(List<TurboLabel> repoLabels, 
                          Map<String, Boolean> groups, String group,
                          List<String> matchedLabels, List<String> assignedLabels,
                          Optional<String> suggestion) {
        Label groupName = new Label(group + (groups.get(group) ? "." : "-"));
        groupName.setPadding(new Insets(0, 5, 5, 0));
        groupName.setMaxWidth(ELEMENT_MAX_WIDTH - 10);
        groupName.setStyle("-fx-font-size: 110%; -fx-font-weight: bold;");

        FlowPane groupPane = new FlowPane();
        groupPane.setHgap(5);
        groupPane.setVgap(5);
        groupPane.setPadding(new Insets(0, 0, 10, 10));
        repoLabels
                .stream()
                .filter(label -> label.getGroup().isPresent())
                .filter(label -> label.getGroup().get().equalsIgnoreCase(group))
                .map(label -> processGroupLabels(label, matchedLabels, 
                                                 assignedLabels, suggestion))
                .forEach(label -> {
                  groupPane.getChildren().add(label);
                });
        feedbackLabels.getChildren().addAll(groupName, groupPane);
    }

    private String getColour(String name, List<TurboLabel> repoLabels) {
        String colour = repoLabels.stream().filter(
            label -> label.getActualName().equals(name)).findAny().get().getColour();
        return colour;
    }

    public String getStyle(String name, List<TurboLabel> repoLabels) {
        String colour = getColour(name, repoLabels);
        int r = Integer.parseInt(colour.substring(0, 2), 16);
        int g = Integer.parseInt(colour.substring(2, 4), 16);
        int b = Integer.parseInt(colour.substring(4, 6), 16);
        double luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b;
        boolean bright = luminance > 128;
        return "-fx-background-color: #" + colour + "; -fx-text-fill: " + (bright ? "black;" : "white;");
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
        
    // TODO determine color of label
    private Label processInitialLabel(Label label, List<String> removedLabels, 
                                     Optional<String> suggestion) {
        // initial label only faded when it is already removed
        if (removedLabels.contains(label.getText())
            && suggestion.isPresent() && suggestion.get().equals(label.getText())) {
            setFadedLabel(label);
        } 
        
        if (suggestion.isPresent() && suggestion.get().equals(label.getText())) {
           setStrikedLabel(label);
           setFadedLabel(label); 
        }
        
        if (removedLabels.contains(label.getText())) {
           setStrikedLabel(label);
        }

        return label;
    }

    private Label createBasicLabel(String name) {
        Label label = new Label(name);
        label.getStyleClass().add("labels");
        FontLoader fontLoader = Toolkit.getToolkit().getFontLoader();
        double width = (double) fontLoader.computeStringWidth(label.getText(), label.getFont());
        label.setPrefWidth(width + 30);
        label.setOnMouseClicked(e -> handleClick(label));
        return label;
    }

    // TODO handling group label text which contains partial name only
    private Label createGroupLabel(String name) {
        Label label = new Label(getName(name));
        label.getStyleClass().add("labels");
        label.setStyle(getStyle(name, this.repoLabels));
        FontLoader fontLoader = Toolkit.getToolkit().getFontLoader();
        double width = (double) fontLoader.computeStringWidth(label.getText(), label.getFont());
        label.setPrefWidth(width + 30);
        label.setOnMouseClicked(e -> handleClick(label, name));
        return label;
    }

    // TODO find a way to increase border-width without breaking the UI
    private void setSuggestedLabel(Label label) {
        String suggestRemoveStyle = label.getStyle() + 
            "-fx-border-color:black; ";
        label.setStyle(suggestRemoveStyle);
    }

    private void setFadedLabel(Label label) {
        String suggestRemoveStyle = label.getStyle() + 
            "-fx-opacity: 40%; ";
        label.setStyle(suggestRemoveStyle);
        return label;
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

    private List<String> getFinalLabels(List<String> initialLabels, 
                                        List<String> addedLabels, 
                                        List<String> removedLabels) {
        return repoLabelsString.stream()
            .filter(label -> !removedLabels.contains(label))
            .filter(label -> initialLabels.contains(label) || addedLabels.contains(label))
            .collect(Collectors.toList());
    }

    private List<String> getGroupNames(Map<String, Boolean> groups) {
        List<String> groupNames = groups.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
        Collections.sort(groupNames);
        return groupNames;
    }

    private String getColour(String name, List<TurboLabel> repoLabels) {
        String colour = repoLabels.stream().filter(
            label -> label.getActualName().equals(name)).findAny().get().getColour();
        return colour;
    }

    private String getStyle(String name, List<TurboLabel> repoLabels) {
        String colour = getColour(name, repoLabels);
        int r = Integer.parseInt(colour.substring(0, 2), 16);
        int g = Integer.parseInt(colour.substring(2, 4), 16);
        int b = Integer.parseInt(colour.substring(4, 6), 16);
        double luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b;
        boolean bright = luminance > 128;
        return "-fx-background-color: #" + colour + "; -fx-text-fill: " + (bright ? "black;" : "white;");
    }


    private String getName(String actualName) {
        if (getDelimiter(actualName).isPresent()) {
            String delimiter = getDelimiter(actualName).get();
            // Escaping due to constants not being valid regexes
            String[] segments = actualName.split("\\" + delimiter);
            assert segments.length >= 1;
            if (segments.length == 1) {
                if (actualName.endsWith(delimiter)) {
                    // group.
                    return "";
                } else {
                    // .name
                    return segments[0];
                }
            } else {
                // group.name
                assert segments.length == 2;
                return segments[1];
            }
        } else {
            // name
            return actualName;
        }
    }

    private Optional<String> getDelimiter(String name) {

        // Escaping due to constants not being valid regexes
        Pattern p = Pattern.compile(String.format("^[^\\%s\\%s]+(\\%s|\\%s)",
            EXCLUSIVE_DELIMITER,
            NONEXCLUSIVE_DELIMITER,
            EXCLUSIVE_DELIMITER,
            NONEXCLUSIVE_DELIMITER));
        Matcher m = p.matcher(name);

        if (m.find()) {
            return Optional.of(m.group(1));
        } else {
            return Optional.empty();
        }
    }
}
