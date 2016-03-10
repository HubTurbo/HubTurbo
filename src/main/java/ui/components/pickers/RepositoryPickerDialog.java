package ui.components.pickers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
    private static final MatchingMode DEFAULT_MATCHING_MODE = MatchingMode.SUBSTRING_MATCHING;

    private ComboBox<String> comboBox;
    private RepositoryPickerState state;

    public RepositoryPickerDialog(Set<String> storedRepos, Stage stage) {
        initUi(stage, storedRepos);
    }

    private void initUi(Stage stage, Set<String> storedRepos) {
        state = new RepositoryPickerState(storedRepos);

        initialiseDialog(stage);
        createComboBox();
        createButtons();

        getDialogPane().setContent(comboBox);
        Platform.runLater(() -> {
            comboBox.getEditor().requestFocus();
            comboBox.show();
        });
    }

    private void initialiseDialog(Stage stage) {
        initOwner(stage);
        initModality(Modality.APPLICATION_MODAL);
        setTitle(REPO_PICKER_TITLE);
    }

    private void createComboBox() {
        comboBox = new ComboBox<>();
        comboBox.setId("repositoryPicker");
        comboBox.setEditable(true);
        comboBox.getItems().addAll(state.getMatchingRepositories("", DEFAULT_MATCHING_MODE));
        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            String repoId = Utility.removeAllWhitespace(newValue);
            if (!repoId.equals(newValue)) {
                comboBox.setValue(repoId);
                return;
            }
        });
        comboBox.setOnKeyReleased(event -> {
            if (isQueryUpdateByComboBoxTraversal(event)) {
                handleComboBoxItemsTraversal();
            } else if (isQueryUpdateByUser(event)) {
                handleUserQueryUpdate();
            }
        });
    }

    private boolean isQueryUpdateByComboBoxTraversal(KeyEvent event) {
        return event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN;
    }

    private void handleComboBoxItemsTraversal() {
        if (!comboBox.isShowing()) {
            comboBox.show();
        }
        comboBox.getEditor().positionCaret(comboBox.getEditor().getText().length());
    }

    private void handleUserQueryUpdate() {
        // hide and show is needed so that the combo box will readjust its row count
        comboBox.hide();
        List<String> matchingRepositories = state.getMatchingRepositories(comboBox.getEditor().getText(),
                                                                            DEFAULT_MATCHING_MODE);
        updateRepositoryList(matchingRepositories);
        if (comboBox.getItems().size() > 0) comboBox.show();
    }

    /**
     * Replaces the items of ComboBox but still retain user input.
     */
    private void updateRepositoryList(List<String> matchingRepositories) {
        String originalQuery = comboBox.getEditor().getText();
        int originalCaretPosition = comboBox.getEditor().getCaretPosition();

        comboBox.setItems(FXCollections.observableArrayList(matchingRepositories));

        comboBox.getEditor().setText(originalQuery);
        comboBox.getEditor().positionCaret(originalCaretPosition);
    }

    private boolean isQueryUpdateByUser(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER || event.getCode().isNavigationKey()) {
            return false;
        }
        // TODO: Support more characters?
        boolean result = false;
        result |= event.getCode().isLetterKey();
        result |= event.getCode().isDigitKey();
        result |= event.getCode() == KeyCode.SLASH;
        result |= event.getCode() == KeyCode.UNDERSCORE;
        result |= event.getCode() == KeyCode.MINUS;
        result |= event.getCode() == KeyCode.BACK_SPACE;
        result |= event.getCode() == KeyCode.DELETE;
        return result;
    }

    private void createButtons() {
        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                String chosenRepo = comboBox.getValue();
                assert chosenRepo != null;
                if (Utility.isWellFormedRepoId(chosenRepo)) {
                    return chosenRepo;
                }
            }
            return null;
        });
    }

}
