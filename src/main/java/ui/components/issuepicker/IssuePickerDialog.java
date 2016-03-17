package ui.components.issuepicker;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;

import backend.resource.MultiModel;
import backend.resource.TurboIssue;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui.GuiElement;
import ui.UI;
import util.HTLog;

/**
 * Serves as a presenter that synchronizes changes in issues with dialog view  
 */
public class IssuePickerDialog extends Dialog<List<String>> {

    private static final int ELEMENT_MAX_WIDTH = 400;
    private static final Insets GROUP_PAD = new Insets(0, 0, 10, 10);
    private static final String TITLE = "Issue Picker";
    private static final String FXML_PATH = "fxml/IssuePickerView.fxml";
    private static final Logger logger = HTLog.get(IssuePickerDialog.class);

    private final MultiModel models;
    private final List<TurboIssue> allIssues;

    private IssuePickerState state;

    @FXML
    private VBox issuepickerLayout;
    @FXML
    private VBox selectedIssues;
    @FXML
    private TextField issuepickerQueryField;
    @FXML
    private VBox suggestedIssues;


    public IssuePickerDialog(Stage stage, MultiModel models) {
        this.models = models;
        this.allIssues = models.getIssues();

        initUI(stage, allIssues);
        Platform.runLater(issuepickerQueryField::requestFocus);
    }

    // UI Initialisation

    @FXML
    public void initialize() {
        issuepickerQueryField.textProperty().addListener(
            (observable, oldText, newText) -> handleUserInput(issuepickerQueryField.getText()));
    }

    private void initUI(Stage stage, List<TurboIssue> issues) {
        initialiseDialog(stage);
        setDialogPaneContent();
        createButtons();

        state = new IssuePickerState(issues, "");
        populateSuggestedIssues(allIssues);
    }

    private void initialiseDialog(Stage stage) {
        initOwner(stage);
        initModality(Modality.APPLICATION_MODAL);
        setTitle(TITLE);
    }

    private void setDialogPaneContent() {
        createMainLayout();
        getDialogPane().setContent(issuepickerLayout);
    }

    /**
     * Populates respective panes with issues that matches current user input
     * @param state
     */
    private final void populatePanes(IssuePickerState state) {
        // Population of UI elements
        populateSelectedIssues(state.getSelectedIssues(), state.getCurrentSuggestion());
        populateSuggestedIssues(state.getSuggestedIssues());

        // Ensures dialog pane resize according to content
        getDialogPane().getScene().getWindow().sizeToScene();
    }

    private void populateSelectedIssues(List<TurboIssue> chosenIssues, Optional<TurboIssue> currentSuggestion) {
        selectedIssues.getChildren().clear();
        Map<String, FlowPane> repoContent = getRepoContent(chosenIssues, currentSuggestion);

        repoContent.entrySet().forEach(entry -> {
            entry.getValue().getChildren().add(0, createRepoTitle(entry.getKey()));
            selectedIssues.getChildren().addAll(entry.getValue());
        });
    }

    private void populateSuggestedIssues(List<TurboIssue> matchedIssues) {
        suggestedIssues.getChildren().clear();
        matchedIssues.stream()
            .forEach(issue -> suggestedIssues.getChildren().add(processSuggestedIssue(issue)));
    }

    private Node processSelectedIssue(TurboIssue issue, Optional<TurboIssue> suggestion) {
        PickerIssue styledIssue = new PickerIssue(issue);

        if (suggestion.isPresent() && suggestion.get().equals(issue)) {
            return styledIssue.faded(true).removed(true).getNode();
        }
        return new PickerIssue(issue).getNode();
    }

    private Node processSuggestedIssue(TurboIssue issue) {
        GuiElement element = new GuiElement(issue, models.getLabelsOfIssue(issue), models.getMilestoneOfIssue(issue),
                                            models.getAssigneeOfIssue(issue), models.getAuthorOfIssue(issue));
        IssueCard card = new IssueCard(element);
        card.setOnMouseClicked(e -> handleIssueClick(issue, card));
        return card;
    }


    // Event handling 

    /**
     * Updates state of the issue picker based on the entire query
     */
    private final void handleUserInput(String query) {
        state = new IssuePickerState(allIssues, query.toLowerCase());
        populatePanes(state);
    }
    
    private void handleIssueClick(TurboIssue issue, IssueCard card) {
        issuepickerQueryField.setDisable(true);
        Platform.runLater(card::requestFocus);
        state.updateSelectedIssues(issue);
        populateSelectedIssues(state.getSelectedIssues(), state.getCurrentSuggestion());
    }

    // UI helper methods

    private void createMainLayout() {
        FXMLLoader loader = new FXMLLoader(UI.class.getResource(FXML_PATH));
        loader.setController(this);
        try {
            issuepickerLayout = (VBox) loader.load();
        } catch (IOException e) {
            logger.error("Failure to load FXML. " + e.getMessage());
            close();
        }
    }

    private void createButtons() {
        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        // defines what happens when user confirms/presses enter
        setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                // Confirms final issue that is suggested to user upon confirmation
                if (!issuepickerQueryField.isDisabled()) issuepickerQueryField.appendText(";");
                return state.getSelectedIssues().stream().map(TurboIssue::toString).collect(Collectors.toList());
            }
            return null;
        });
    }

    private FlowPane createRepoPane(Insets padding) {
        FlowPane repo = new FlowPane();
        repo.setHgap(5);
        repo.setVgap(5);
        repo.setPadding(padding);
        return repo;
    }

    private Label createRepoTitle(String name) {
        Label repoName = new Label(name + ": ");
        repoName.setPadding(new Insets(0, 5, 5, 0));
        repoName.setMaxWidth(ELEMENT_MAX_WIDTH - 10);
        repoName.setStyle("-fx-font-size: 110%; -fx-font-weight: bold; ");
        return repoName;
    }

    private final Map<String, FlowPane> getRepoContent(List<TurboIssue> chosenIssues, Optional<TurboIssue> suggestion) {
        Map<String, FlowPane> repoContent = new HashMap<>();
        chosenIssues.stream()
            .forEach(issue -> {
                String repoId = issue.getRepoId();
                if (!repoContent.containsKey(repoId)) {
                    repoContent.put(repoId, createRepoPane(GROUP_PAD));
                }
                repoContent.get(repoId).getChildren().add(processSelectedIssue(issue, suggestion));
            });
        
        // adds suggested issue if not present in selected issues
        if (canAddSuggestion(chosenIssues, suggestion)) addCurrentSuggestion(repoContent, suggestion.get());
        return repoContent;
    }

    /**
     * Adds suggested issue to selected issue 
     * @param repoContent
     * @param chosenIssues
     * @param issue
     */
    private void addCurrentSuggestion(Map<String, FlowPane> repoContent, TurboIssue issue) {
        Node styledIssue = new PickerIssue(issue).faded(true).getNode();

        if (!repoContent.containsKey(issue.getRepoId())) {
            FlowPane repoContentPane = createRepoPane(GROUP_PAD);
            repoContentPane.getChildren().add(styledIssue);
            repoContent.put(issue.getRepoId(), repoContentPane);
            return;
        }
        repoContent.get(issue.getRepoId()).getChildren().add(styledIssue);
    }
    
    /**
     * Suggestion not added if text field is disabled because user has not confirmed addition of the issue
     * @param chosenIssues
     * @param suggestion
     * @return
     */
    private boolean canAddSuggestion(List<TurboIssue> chosenIssues, Optional<TurboIssue> suggestion) {
        return !issuepickerQueryField.isDisabled() 
            && suggestion.isPresent() && !chosenIssues.contains(suggestion.get());
    }
}
