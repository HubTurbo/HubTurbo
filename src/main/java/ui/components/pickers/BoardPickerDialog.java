package ui.components.pickers;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.Logger;
import ui.UI;
import util.HTLog;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static javafx.scene.input.KeyCode.*;

/**
 * @author Liu Xinan
 */
public class BoardPickerDialog extends Dialog<String> {

    private static final Logger logger = HTLog.get(BoardPickerDialog.class);
    private static final String DIALOG_TITLE = "Switch to board...";

    private final List<String> boards;
    private BoardPickerState state;
    private Optional<String> suggestion = Optional.empty();
    private Button confirmButton;

    @FXML
    private VBox mainLayout;

    @FXML
    private TextField queryField;

    @FXML
    private VBox boardList;

    BoardPickerDialog(List<String> boards, Stage stage) {
        this.boards = boards;

        initUI(stage);
        Platform.runLater(queryField::requestFocus);
    }

    @FXML
    public void initialize() {
        queryField.textProperty().addListener(
            (observable, oldText, newText) -> handleUserInput(queryField.getText()));
    }

    private void initUI(Stage stage) {
        initializeDialog(stage);
        setDialogPaneContent();
        createButtons();

        state = new BoardPickerState(new HashSet<>(boards), "");
        populateBoards(state);
    }

    private void initializeDialog(Stage stage) {
        initOwner(stage);
        initModality(Modality.APPLICATION_MODAL);
        setTitle(DIALOG_TITLE);
    }

    private void setDialogPaneContent() {
        setMainLayout();
        getDialogPane().setContent(mainLayout);
    }

    private void setMainLayout() {
        FXMLLoader loader = new FXMLLoader(UI.class.getResource("fxml/BoardPickerView.fxml"));
        loader.setController(this);
        try {
            mainLayout = loader.load();
        } catch (IOException e) {
            logger.error("Failed to load FXML. " + e.getMessage());
            close();
        }
    }

    private void createButtons() {
        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        confirmButton = (Button) getDialogPane().lookupButton(confirmButtonType);

        setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return suggestion.orElse(null);
            }
            return null;
        });
    }

    private void populateBoards(BoardPickerState state) {
        boardList.getChildren().clear();

        state.getMatchedBoards().stream()
                .sorted(String::compareToIgnoreCase)
                .map(PickerBoard::new)
                .forEach(pickerBoard -> {
                    suggestion.ifPresent(suggestion -> {
                        pickerBoard.setHighlighted(suggestion.equals(pickerBoard.getBoardName()));
                    });
                    pickerBoard.setOnMouseClicked(e -> handleBoardClick(pickerBoard));
                    boardList.getChildren().add(pickerBoard);
                });

        updateConfirmButton();
    }

    private void updateConfirmButton() {
        if (suggestion.isPresent()) {
            confirmButton.setDisable(false);
        } else {
            confirmButton.setDisable(true);
        }
    }

    private final void handleUserInput(String query) {
        state = new BoardPickerState(new HashSet<>(boards), query.toLowerCase());
        suggestion = state.getSuggestion();
        populateBoards(state);
    }

    private void handleBoardClick(PickerBoard board) {
        suggestion = Optional.of(board.getBoardName());
        queryField.setText(suggestion.get());
        updateConfirmButton();
    }
}
