package ui.components.pickers;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui.IdGenerator;
import util.Utility;

import java.util.*;

/**
 * Serves as a presenter that also helps the user by suggesting repositories stored in the disk
 * which are related to user input.
 */
public class RepositoryPickerDialog extends Dialog<String> {

    private static final String REPO_PICKER_TITLE = "Pick another repository";
    private static final int DEFAULT_SUGGESTED_REPO_LIST_HEIGHT = 350;
    private static final int DEFAULT_SUGGESTED_REPO_LIST_WIDTH = 350;
    private static final int SPACING_BETWEEN_NODES = 10;
    private static final Insets DEFAULT_PADDING = new Insets(10);
    private static final Insets DEFAULT_LABEL_MARGIN = new Insets(2);

    private VBox mainLayout;
    private VBox suggestedRepositoryList;
    private TextField userInputTextField;
    private RepositoryPickerState state;

    public RepositoryPickerDialog(Set<String> storedRepos, Stage stage) {
        initUi(stage, storedRepos);
    }

    private void initUi(Stage stage, Set<String> storedRepos) {
        state = new RepositoryPickerState(storedRepos);

        initialiseDialog(stage);
        createButtons();
        initialiseDefaultValues();

        Platform.runLater(() -> userInputTextField.requestFocus());
    }

    /**
     * Assumes empty to be the default user query. This in turn also adds all existing repositories in form of label
     * into suggestedRepositoryList.
     */
    private void initialiseDefaultValues() {
        updateUserQuery("");
    }

    private void initialiseDialog(Stage stage) {
        initOwner(stage);
        initModality(Modality.APPLICATION_MODAL);
        setTitle(REPO_PICKER_TITLE);

        createMainLayout();
        getDialogPane().setContent(mainLayout);
    }

    private void createMainLayout() {
        mainLayout = new VBox(SPACING_BETWEEN_NODES);
        mainLayout.setPadding(DEFAULT_PADDING);

        createSuggestedRepositoriesList();
        createUserInputTextField();
        registerEventHandlers();

        ScrollPane suggestedRepositoryListScrollPane = new ScrollPane(suggestedRepositoryList);
        suggestedRepositoryListScrollPane.setFitToHeight(true);
        suggestedRepositoryListScrollPane.setFitToWidth(true);
        mainLayout.getChildren().addAll(userInputTextField, suggestedRepositoryListScrollPane);
    }

    private void registerEventHandlers() {
        userInputTextField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DOWN) {
                state.selectNextSuggestedRepository();
                e.consume();
            } else if (e.getCode() == KeyCode.UP) {
                state.selectPreviousSuggestedRepository();
                e.consume();
            }
            updateSuggestedRepositoryList();
        });
    }

    private void updateSuggestedRepositoryList() {
        suggestedRepositoryList.getChildren().clear();
        List<PickerRepository> suggestedRepositories = state.getSuggestedRepositories();
        suggestedRepositories.stream()
                .forEach(repo -> {
                    Node repoLabel = repo.getNode();
                    suggestedRepositoryList.getChildren().add(repoLabel);
                    suggestedRepositoryList.setMargin(repoLabel, DEFAULT_LABEL_MARGIN);
                    repoLabel.setOnMouseClicked(e -> handleMouseClick(repo.getRepositoryId()));
                });
    }

    private void createSuggestedRepositoriesList() {
        suggestedRepositoryList = new VBox();
        suggestedRepositoryList.setPadding(DEFAULT_PADDING);
        suggestedRepositoryList.setId(IdGenerator.getRepositoryPickerSuggestedRepoListId());
        suggestedRepositoryList.setStyle("-fx-background-color: white; -fx-border-color:black;");
        suggestedRepositoryList.setPrefHeight(DEFAULT_SUGGESTED_REPO_LIST_HEIGHT);
        suggestedRepositoryList.setPrefWidth(DEFAULT_SUGGESTED_REPO_LIST_WIDTH);
    }

    private void handleMouseClick(String repositoryId) {
        userInputTextField.setDisable(true);
        state.setSelectedRepositoryInSuggestedList(repositoryId);
        updateSuggestedRepositoryList();
    }

    private void updateUserQuery(String query) {
        suggestedRepositoryList.getChildren().clear();
        state.processUserQuery(query);
        updateSuggestedRepositoryList();
    }

    private void createUserInputTextField() {
        userInputTextField = new TextField();
        userInputTextField.setId(IdGenerator.getRepositoryPickerTextFieldId());
        userInputTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            String repositoryId = Utility.removeAllWhitespace(newValue);
            if (!repositoryId.equals(newValue)) {
                userInputTextField.setText(repositoryId);
                return;
            }

            updateUserQuery(newValue);
        });
    }

    private void createButtons() {
        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        setResultConverter(dialogButton -> {
            if (!dialogButton.equals(confirmButtonType)) {
                return null;
            }

            Optional<String> selectedRepositoryId = state.getSelectedRepositoryId();
            if (!selectedRepositoryId.isPresent()) {
                return null;
            }

            return Utility.isWellFormedRepoId(selectedRepositoryId.get()) ? selectedRepositoryId.get() : null;
        });
    }

}
