package ui.components.pickers;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
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

/**
 * @author Liu Xinan
 */
public class BoardPickerDialog extends Dialog<String> {

    private static final Logger logger = HTLog.get(BoardPickerDialog.class);
    private static final String DIALOG_TITLE = "Switch to board...";

    private List<String> boards;
    private ObservableList<PickerBoard> pickerBoards;
    private BoardPickerState state;
    private Optional<String> suggestion = Optional.empty();
    private ButtonType confirmButtonType;

    @FXML
    private VBox mainLayout;

    @FXML
    private Label title;

    @FXML
    private TextField queryField;

    @FXML
    private ListView<PickerBoard> boardList;

    @FXML
    private VBox boardNames;

    BoardPickerDialog(List<String> boards, Stage stage) {
        this.boards = boards;
        this.pickerBoards = FXCollections.observableArrayList();

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
        setMainLayout();
        createButtons();

        boardList.setItems(pickerBoards);
        state = new BoardPickerState(new HashSet<>(boards), "");
        populateBoards(state);
    }

    private void initializeDialog(Stage stage) {
        initOwner(stage);
        initModality(Modality.APPLICATION_MODAL);
        setTitle(DIALOG_TITLE);
    }

    private void setMainLayout() {
        FXMLLoader loader = new FXMLLoader(UI.class.getResource("fxml/BoardPickerView.fxml"));
        loader.setController(this);
        try {
            mainLayout = loader.load();
            getDialogPane().setContent(mainLayout);
        } catch (IOException e) {
            logger.error("Failed to load FXML. " + e.getMessage());
            close();
        }
    }

    private void createButtons() {
        confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return suggestion.orElse(null);
            }
            return null;
        });
    }

    private void populateBoards(BoardPickerState state) {
        pickerBoards.clear();

        state.getMatchedBoards().stream()
                .sorted(String::compareToIgnoreCase)
                .map(PickerBoard::new)
                .forEach(pickerBoard -> {
                    suggestion.ifPresent(suggestion -> {
                        pickerBoard.highlighted(suggestion.equals(pickerBoard.getBoardName()));
                    });
                    pickerBoard.setPrefHeight(40);
                    pickerBoard.setOnMouseClicked(e -> handleBoardClick(pickerBoard));
                    pickerBoards.add(pickerBoard);
                });

        getDialogPane().getScene().getWindow().sizeToScene();
        if (suggestion.isPresent()) {
            getDialogPane().lookupButton(confirmButtonType).setDisable(false);
            boardList.getSelectionModel().select(0);
            System.out.println(boardList.getHeight());
            boardList.getItems().forEach(item -> System.out.println(item.getHeight()));
        } else {
            getDialogPane().lookupButton(confirmButtonType).setDisable(true);
        }
    }

    private final void handleUserInput(String query) {
        state = new BoardPickerState(new HashSet<>(boards), query.toLowerCase());
        suggestion = state.getSuggestion();
        populateBoards(state);
    }

    private void handleBoardClick(PickerBoard board) {
        suggestion = Optional.of(board.getBoardName());
        getDialogPane().lookupButton(confirmButtonType).setDisable(false);
    }
}
