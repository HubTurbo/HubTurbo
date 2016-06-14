package ui.components.pickers;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import prefs.RepoInfo;
import ui.IdGenerator;
import ui.UI;
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

    public RepositoryPickerDialog(List<RepoInfo> storedRepos, Consumer<Optional<String>> onCloseCallback,
                                  Function<String, CompletableFuture<Boolean>> repoValidator) {
        this.onCloseCallback = onCloseCallback;
        this.repoValidator = repoValidator;
        initUi(storedRepos);
    }

    private void initUi(List<RepoInfo> storedRepos) {
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
     * Initialises the dialog for adding a repository.
     * User can add both the repo id and the optional repo alias.
     *
     * @return the RepoInfo object constructed from the user input
     */
    private Dialog<RepoInfo> initAddRepositoryDialog() {
        // Adapted from http://code.makery.ch/blog/javafx-dialogs-official/

        Dialog<RepoInfo> dialog = new Dialog<>();
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setHeaderText("Add a new repository");

        // Set the button types.
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Create the repo id and alias labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField repoIdField = new TextField();
        repoIdField.setPromptText("Repository ID");
        TextField repoAliasField = new TextField();
        repoAliasField.setPromptText("Alias (Optional)");

        grid.add(new Label("Repository ID:"), 0, 0);
        grid.add(repoIdField, 1, 0);
        grid.add(new Label("Alias (Optional):"), 0, 1);
        grid.add(repoAliasField, 1, 1);

        // Enable/Disable add button depending on whether a repo id was entered.
        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);

        // Sets the repo id to the search term automatically
        repoIdField.setText(userInputTextField.getText());
        repoIdField.selectAll();

        dialog.getDialogPane().setContent(grid);

        // Validation on repo id.
        repoIdField.textProperty().addListener((observable, oldValue, newValue) -> {
            // disable add button if no repo id
            addButton.setDisable(newValue.trim().isEmpty());

            // prevent user from typing whitespace in repo id
            String repositoryId = Utility.removeAllWhitespace(newValue);
            if (!repositoryId.equals(newValue)) {
                repoIdField.setText(repositoryId);
            }
        });

        // Validation on repo alias.
        repoAliasField.textProperty().addListener((observable, oldValue, newValue) -> {
            // prevent user from typing whitespace in repo alias
            String repositoryAlias = Utility.removeAllWhitespace(newValue);
            if (!repositoryAlias.equals(newValue)) {
                repoIdField.setText(repositoryAlias);
            }
        });

        // Request focus on the repo id field by default.
        Platform.runLater(() -> repoIdField.requestFocus());

        // Convert the result to a RepoInfo object when the add button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new RepoInfo(repoIdField.getText(), repoAliasField.getText());
            }
            return null;
        });

        return dialog;
    }

    /**
     * Prompts the user for a repository id then return it only if it is a valid github repo. This method also informs
     * the user if the repository that the user is trying to add is not valid.
     */
    private CompletableFuture<Boolean> addRepository() {
        Dialog<RepoInfo> dialog = initAddRepositoryDialog();
        Optional<RepoInfo> userInput = dialog.showAndWait();
        if (!userInput.isPresent()) {
            return CompletableFuture.completedFuture(false);
        }

        RepoInfo repoToAdd = userInput.get();
        String repoId = repoToAdd.getId();
        String repoAlias = repoToAdd.getAlias();
        return repoValidator.apply(repoId)
                .thenCompose(valid -> {
                    // disallow adding if repo is invalid
                    if (!valid) {
                        Platform.runLater(() ->
                            DialogMessage.showErrorDialog("Invalid Repository ID",
                                    "\"" + repoId + "\" is not a valid GitHub repository. " +
                                    "Please check again. \n\nRepository not added.")
                        );
                        return Futures.unit(false);
                    }

                    // disallow adding if alias does not match format
                    if (!Utility.isWellFormedRepoAlias(repoAlias)) {
                        Platform.runLater(() ->
                            DialogMessage.showErrorDialog("Invalid Repository Alias",
                                    "\"" + repoAlias + "\" is not a valid Repository Alias. " +
                                    "Please use only alphanumeric characters. \n\nRepository not added.")
                        );
                        return Futures.unit(false);
                    }

                    // disallow adding if alias already exists
                    if (UI.prefs.getRepoByAlias(repoAlias).isPresent()) {
                        Platform.runLater(() ->
                            DialogMessage.showErrorDialog("Repository Alias Already Exists",
                                    "\"" + repoAlias + "\" is already an alias of another repository. " +
                                    "Please choose another alias. \n\nRepository not added.")
                        );
                    }

                    // add to the dialog state (non persistent)
                    Platform.runLater(() -> addRepositoryToState(userInput.get()));
                    // add to the prefs repo list (persistent)
                    UI.prefs.addRepo(repoToAdd);
                    return CompletableFuture.completedFuture(true);
                }).exceptionally(e -> {
                    Platform.runLater(() -> DialogMessage.showErrorDialog("Error checking the validity of " + repoId,
                                                                          "Please try again."));
                    return withResult(false).apply(e);
                });
    }

    private void addRepositoryToState(RepoInfo repo) {
        state.addRepository(repo);
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
