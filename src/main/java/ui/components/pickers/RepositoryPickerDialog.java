package ui.components.pickers;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui.IdGenerator;
import util.DialogMessage;
import util.Futures;
import util.Utility;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import static util.Futures.withResult;

/**
 * Serves as a presenter that also helps the user by suggesting repositories stored in the disk
 * which are related to user input.
 */
public class RepositoryPickerDialog {

    private static final String REPO_PICKER_TITLE = "Pick another repository";
    private static final int DEFAULT_SUGGESTED_REPO_LIST_HEIGHT = 350;
    private static final int DEFAULT_SUGGESTED_REPO_LIST_WIDTH = 350;
    private static final int SPACING_BETWEEN_NODES = 10;
    private static final Insets DEFAULT_PADDING = new Insets(10);
    private static final Insets DEFAULT_LABEL_MARGIN = new Insets(2);

    private final Consumer<Optional<String>> onCloseCallback;
    private final Function<String, CompletableFuture<Boolean>> repoValidator;
    private final Stage stage = new Stage();

    private VBox suggestedRepositoryList;
    private HBox buttons;
    private TextField userInputTextField;
    private RepositoryPickerState state;

    public RepositoryPickerDialog(Set<String> storedRepos, Consumer<Optional<String>> onCloseCallback,
                                  Function<String, CompletableFuture<Boolean>> repoValidator) {
        this.onCloseCallback = onCloseCallback;
        this.repoValidator = repoValidator;
        initUi(storedRepos);
    }

    private void initUi(Set<String> storedRepos) {
        state = new RepositoryPickerState(storedRepos);

        initialiseDialog();
        initialiseDefaultValues();

        Platform.runLater(() -> {
            stage.showAndWait();
            userInputTextField.requestFocus();
        });
    }

    /**
     * Assumes empty to be the default user query. This in turn also adds all existing repositories in form of label
     * into suggestedRepositoryList.
     */
    private void initialiseDefaultValues() {
        updateUserQuery("");
    }

    private void initialiseDialog() {
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(REPO_PICKER_TITLE);

        createMainLayout();
        registerEventHandlers();
    }

    private void createMainLayout() {
        VBox mainLayout = new VBox(SPACING_BETWEEN_NODES);
        mainLayout.setPadding(DEFAULT_PADDING);

        createButtons();
        createSuggestedRepositoriesList();
        createUserInputTextField();

        ScrollPane suggestedRepositoryListScrollPane = new ScrollPane(suggestedRepositoryList);
        suggestedRepositoryListScrollPane.setFitToHeight(true);
        suggestedRepositoryListScrollPane.setFitToWidth(true);
        mainLayout.getChildren().addAll(userInputTextField, suggestedRepositoryListScrollPane, buttons);
        stage.setScene(new Scene(mainLayout));
    }

    private void registerEventHandlers() {
        stage.getScene().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                quitRepositoryPicker();
                e.consume();
            } else if (e.getCode() == KeyCode.ENTER) {
                confirmSelectedRepository();
                e.consume();
            }
        });
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

    /**
     * Prompts the user for a repository id then return it only if it is a valid github repo. This method also informs
     * the user if the repository that the user is trying to add is not valid.
     */
    private CompletableFuture<Boolean> addRepository() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Add a new repository");
        dialog.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            String repositoryId = Utility.removeAllWhitespace(newValue);
            if (!repositoryId.equals(newValue)) {
                dialog.getEditor().setText(repositoryId);
                return;
            }
        });
        Optional<String> userInput = dialog.showAndWait();
        if (!userInput.isPresent()) {
            return CompletableFuture.completedFuture(false);
        }

        String repoId = userInput.get();
        return repoValidator.apply(repoId)
                .thenCompose(valid -> {
                    if (!valid) {
                        Platform.runLater(() -> DialogMessage.showErrorDialog("Cannot add to repository list",
                                repoId + " is not a valid GitHub repository."));
                        return Futures.unit(false);
                    }

                    Platform.runLater(() -> addRepositoryToState(repoId));
                    return CompletableFuture.completedFuture(true);
                }).exceptionally(e -> {
                    Platform.runLater(() -> DialogMessage.showErrorDialog("Error checking the validity of " + repoId,
                                                                          "Please try again."));
                    return withResult(false).apply(e);
                });
    }

    private void addRepositoryToState(String repoId) {
        state.addRepository(repoId);
        updateUserQuery(userInputTextField.getText());
    }

    private void createButtons() {
        buttons = new HBox(SPACING_BETWEEN_NODES);
        buttons.setAlignment(Pos.BOTTOM_RIGHT);
        Button confirm = new Button("Confirm");
        Button cancel = new Button("Cancel");
        Button addRepository = new Button("Add repository");
        buttons.getChildren().addAll(addRepository, cancel, confirm);

        confirm.setDefaultButton(true);
        confirm.setOnAction((event) -> confirmSelectedRepository());
        cancel.setOnAction((event) -> quitRepositoryPicker());
        addRepository.setOnAction((event) -> addRepository());
    }

    private void confirmSelectedRepository() {
        Optional<String> selectedRepositoryId = state.getSelectedRepositoryId();
        if (!selectedRepositoryId.isPresent()) {
            quitRepositoryPicker();
            return;
        }

        String result = Utility.isWellFormedRepoId(selectedRepositoryId.get()) ? selectedRepositoryId.get() : null;
        onCloseCallback.accept(Optional.ofNullable(result));
        quitRepositoryPicker();
    }

    private void quitRepositoryPicker() {
        stage.close();
    }

}
