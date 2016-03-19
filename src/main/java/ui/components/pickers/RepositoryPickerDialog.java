package ui.components.pickers;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import util.Utility;

import java.util.*;

/**
 * Serves as a presenter that helps the user by suggesting repositories stored in the disk
 * which are related to user input.
 */
public class RepositoryPickerDialog extends Dialog<String> {

    private static final String REPO_PICKER_TITLE = "Pick another repository";
    private static final String DEFAULT_SELECTED_REPO_MESSAGE = "Selected repository: ";
    private static final int DEFAULT_MATCHING_REPO_LIST_HEIGHT = 350;
    private static final int DEFAULT_MATCHING_REPO_LIST_WIDTH = 350;
    private static final int SPACING_BETWEEN_NODES = 10;
    private static final Insets DEFAULT_PADDING = new Insets(10);
    private static final Insets DEFAULT_MARGIN = new Insets(5);

    private VBox mainLayout;
    private VBox matchingRepositoryList;
    private HBox chosenRepository;
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
        createHandlers();
        createChosenRepository();

        initialiseDefaultValues();

        ScrollPane scrollPane = new ScrollPane(matchingRepositoryList);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        mainLayout.getChildren().addAll(userInputTextField, scrollPane, chosenRepository);
    }

    private void createHandlers() {
        userInputTextField.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.DOWN) {
                state.selectNextMatchingRepository();
            } else if (e.getCode() == KeyCode.UP) {
                state.selectPreviousMatchingRepository();
            }
            refreshUi();
        });
    }

    private void refreshUi() {
        updateMatchingRepositoryList();
        updateSelectedRepository();
    }

    private void updateMatchingRepositoryList() {
        matchingRepositoryList.getChildren().clear();
        List<PickerRepository> matchingRepositories = state.getMatchingRepositories();
        matchingRepositories.stream()
                .sorted()
                .forEach(repo -> {
                    Node repoLabel = repo.getNode();
                    VBox.setMargin(repoLabel, DEFAULT_MARGIN);
                    matchingRepositoryList.getChildren().add(repoLabel);
                    repoLabel.setOnMouseClicked(e -> handleMouseClick(repo.getRepositoryId()));
                });
    }

    private void updateSelectedRepository() {
        chosenRepository.getChildren().clear();
        chosenRepository.getChildren().add(new Label(DEFAULT_SELECTED_REPO_MESSAGE + state.getSelectedRepositoryId()));
    }

    private void initialiseDefaultValues() {
        updateUserQuery("");
    }

    private void createMatchingRepositoriesList() {
        matchingRepositoryList = new VBox();
        matchingRepositoryList.setId("matchingRepositoryList");
        matchingRepositoryList.setPadding(DEFAULT_PADDING);
        matchingRepositoryList.setPrefHeight(DEFAULT_MATCHING_REPO_LIST_HEIGHT);
        matchingRepositoryList.setPrefWidth(DEFAULT_MATCHING_REPO_LIST_WIDTH);
    }

    private void createChosenRepository() {
        chosenRepository = new HBox();
    }

    private void handleMouseClick(String repositoryId) {
        userInputTextField.setDisable(true);
        updateUserQuery(repositoryId);
    }

    private void updateUserQuery(String query) {
        matchingRepositoryList.getChildren().clear();
        state.updateUserQuery(query);
        refreshUi();
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
            if (dialogButton != confirmButtonType) {
                return null;
            }

            String selectedRepositoryId = state.getSelectedRepositoryId();
            return Utility.isWellFormedRepoId(selectedRepositoryId) ? selectedRepositoryId : null;
        });
    }

}
