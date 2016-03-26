package ui.components.pickers;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import util.Utility;

import java.util.*;

/**
 * Serves as a presenter that also helps the user by suggesting repositories stored in the disk
 * which are related to user input.
 */
public class RepositoryPickerDialog extends Dialog<String> {

    private static final String REPO_PICKER_TITLE = "Pick another repository";
    private static final int DEFAULT_MATCHING_REPO_LIST_HEIGHT = 350;
    private static final int DEFAULT_MATCHING_REPO_LIST_WIDTH = 350;
    private static final int SPACING_BETWEEN_NODES = 10;
    private static final Insets DEFAULT_PADDING = new Insets(10);
    private static final Insets DEFAULT_LABEL_MARGIN = new Insets(2);

    private VBox mainLayout;
    private VBox matchingRepositoryList;
    private TextField userInputTextField;
    private RepositoryPickerState state;

    public RepositoryPickerDialog(Set<String> storedRepos, Stage stage) {
        initUi(stage, storedRepos);
    }

    private void initUi(Stage stage, Set<String> storedRepos) {
        state = new RepositoryPickerState(storedRepos);

        initialiseDialog(stage);
        createButtons();

        Platform.runLater(() -> userInputTextField.requestFocus());
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

        createMatchingRepositoriesList();
        createUserInputTextField();
        registerEventHandlers();

        ScrollPane matchingRepositoryListScrollPane = new ScrollPane(matchingRepositoryList);
        matchingRepositoryListScrollPane.setFitToHeight(true);
        matchingRepositoryListScrollPane.setFitToWidth(true);
        mainLayout.getChildren().addAll(userInputTextField, matchingRepositoryListScrollPane);
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
        matchingRepositoryList.getChildren().clear();
        List<PickerRepository> matchingRepositories = state.getSuggestedRepositories();
        matchingRepositories.stream()
                .forEach(repo -> {
                    Node repoLabel = repo.getNode();
                    matchingRepositoryList.getChildren().add(repoLabel);
                    matchingRepositoryList.setMargin(repoLabel, DEFAULT_LABEL_MARGIN);
                    repoLabel.setOnMouseClicked(e -> handleMouseClick(repo.getRepositoryId()));
                });
    }

    private void createMatchingRepositoriesList() {
        matchingRepositoryList = new VBox();
        matchingRepositoryList.setPadding(DEFAULT_PADDING);
        matchingRepositoryList.setId("matchingRepositoryList");
        matchingRepositoryList.setStyle("-fx-background-color: white; -fx-border-color:black;");
        matchingRepositoryList.setPrefHeight(DEFAULT_MATCHING_REPO_LIST_HEIGHT);
        matchingRepositoryList.setPrefWidth(DEFAULT_MATCHING_REPO_LIST_WIDTH);
    }

    private void handleMouseClick(String repositoryId) {
        userInputTextField.setDisable(true);
        state.setSelectedRepositoryInSuggestedList(repositoryId);
        updateSuggestedRepositoryList();
    }

    private void updateUserQuery(String query) {
        matchingRepositoryList.getChildren().clear();
        state.processUserQuery(query);
        updateSuggestedRepositoryList();
    }

    private void createUserInputTextField() {
        userInputTextField = new TextField();
        userInputTextField.setId("repositoryPickerUserInputField");
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

            String selectedRepositoryId = state.getSelectedRepositoryId();
            return Utility.isWellFormedRepoId(selectedRepositoryId) ? selectedRepositoryId : null;
        });
    }

}
