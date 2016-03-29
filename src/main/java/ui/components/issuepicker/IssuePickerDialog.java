package ui.components.issuepicker;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

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
public class IssuePickerDialog extends Dialog<String> {

    private static final int ELEMENT_MAX_WIDTH = 400;
    private static final Insets GROUP_PAD = new Insets(0, 0, 10, 10);
    private static final String TITLE = "Issue Picker";
    private static final String FXML_PATH = "fxml/IssuePickerView.fxml";
    private static final Logger logger = HTLog.get(IssuePickerDialog.class);
    private static final int MAX_ISSUE_SIZE = 40;

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
        populateSuggestedIssues(allIssues, state.getSelectedIssue());
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
     *
     * @param state
     */
    private final void populatePanes(IssuePickerState state) {
        // Population of UI elements
        populateSelectedIssues(state.getSelectedIssue());
        populateSuggestedIssues(state.getSuggestedIssues(), state.getSelectedIssue());

        // Ensures dialog pane resize according to content
        getDialogPane().getScene().getWindow().sizeToScene();
    }

    private void populateSelectedIssues(Optional<TurboIssue> selectedIssue) {
        selectedIssues.getChildren().clear();
        FlowPane selectedIssueCards = getSelectedIssuesPane(selectedIssue);
        if (!selectedIssueCards.getChildren().isEmpty()) {
            selectedIssues.getChildren().addAll(createRepoTitle(models.getDefaultRepo()), selectedIssueCards);
        }
    }

    private void populateSuggestedIssues(List<TurboIssue> matchedIssues, Optional<TurboIssue> selectedIssue) {
        suggestedIssues.getChildren().clear();
        matchedIssues.stream().limit(MAX_ISSUE_SIZE)
                .forEach(issue -> suggestedIssues.getChildren().add(processIssue(issue, selectedIssue)));
    }

    private Node processIssue(TurboIssue issue, Optional<TurboIssue> selectedIssue) {
        GuiElement element = new GuiElement(issue, models.getLabelsOfIssue(issue), models.getMilestoneOfIssue(issue),
                                            models.getAssigneeOfIssue(issue), models.getAuthorOfIssue(issue));
        IssueCard card = new IssueCard(element, isSuggestedIssue(issue, selectedIssue));
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
        populateSelectedIssues(Optional.of(issue));
        populateSuggestedIssues(state.getSuggestedIssues(), Optional.of(issue));
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
            Optional<TurboIssue> selectedIssue = state.getSelectedIssue();
            if (dialogButton.getButtonData() == ButtonData.OK_DONE && selectedIssue.isPresent()) {
                return selectedIssue.get().toString();
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

    private final FlowPane getSelectedIssuesPane(Optional<TurboIssue> chosenIssues) {
        FlowPane pane = createRepoPane(GROUP_PAD);
        chosenIssues.ifPresent(issue -> pane.getChildren().add(processIssue(issue, Optional.of(issue))));
        return pane;
    }

    private boolean isSuggestedIssue(TurboIssue issue, Optional<TurboIssue> currentSuggestion) {
        return currentSuggestion.isPresent() && currentSuggestion.get().equals(issue);
    }
}
