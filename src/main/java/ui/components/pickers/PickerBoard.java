package ui.components.pickers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import ui.UI;

/**
 * Represent a candidate board UI element in the candidate list of BoardPickerDialog
 *
 * It contains the board name, and a method to toggle its highlighted state.
 */
public class PickerBoard extends HBox {

    @FXML
    private Label name;

    private final String boardName;

    public PickerBoard(String boardName) {
        this.boardName = boardName;

        loadView();
        setHighlighted(false);
    }

    private void loadView() {
        try {
            FXMLLoader loader = new FXMLLoader(UI.class.getResource("fxml/PickerBoardItem.fxml"));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getBoardName() {
        return boardName;
    }

    public final void setHighlighted(boolean isHighlighted) {
        if (isHighlighted) {
            name.setText(boardName + " ✓");
        } else {
            name.setText(boardName);
        }
    }

}
