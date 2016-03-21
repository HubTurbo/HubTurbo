package ui.components.pickers;

import javafx.application.Platform;
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
    private BoardPickerState state;
    private Optional<String> suggestion = Optional.empty();

    @FXML
    private VBox mainLayout;

    @FXML
    private Label title;

    @FXML
    private TextField queryField;

    @FXML
    private VBox boardList;

    @FXML
    private VBox boardNames;

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
        setMainLayout();
        createButtons();

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
        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                // Ensures the last keyword in the query is toggled after confirmation
                return suggestion.orElse(null);
            }
            return null;
        });
    }

    private void populateBoards(BoardPickerState state) {
        boardNames.getChildren().clear();
        boards.stream()
                    .map(boardName -> {
                        PickerBoard pb = new PickerBoard(boardName);
                        pb.faded(!state.getMatchedBoards().contains(boardName));
                        pb.highlighted(suggestion.isPresent() && suggestion.get().equals(boardName));
                        return pb;
                    })
                    .forEach(boardName -> boardNames.getChildren().add(getPickerBoardNode(boardName)));

        getDialogPane().getScene().getWindow().sizeToScene();
    }

    private final Node getPickerBoardNode(PickerBoard board) {
        Node node = board.getNode();
        node.setOnMouseClicked(e -> handleBoardClick(board));
        return node;
    }

    private final void handleUserInput(String query) {
        state = new BoardPickerState(new HashSet<>(boards), query.toLowerCase());
        suggestion = state.getSuggestion();
        populateBoards(state);
    }

    private void handleBoardClick(PickerBoard board) {
        // TODO
    }
}
