package ui.components.pickers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.spreadsheet.Picker;
import ui.UI;
import util.HTLog;

import java.io.IOException;

/**
 * Represents a candidate board UI element in the candidate list of BoardPickerDialog
 */
public class PickerBoard extends HBox {

    private static final Logger logger = HTLog.get(PickerBoard.class);

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
        } catch (IOException e) {
            logger.error("Failure to load FXML. " + e.getMessage());
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
