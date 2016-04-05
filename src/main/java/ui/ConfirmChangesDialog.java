package ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

/**
 * This class represents a confirm dialog that asks the user whether or not to save the current board before
 * switching to another board, or creating a new board.
 */
public class ConfirmChangesDialog extends Alert {

    private static final String DIALOG_TITLE = "Save Changes?";
    private static final String DIALOG_HEADER = "There are unsaved changes to your current board.";
    private static final String DIALOG_CONTENT = "If you choose not to save, all unsaved changes will be discarded.\n\n"
                                               + "Do you want to save them?";

    public ConfirmChangesDialog(Stage stage) {
        super(AlertType.CONFIRMATION);
        initOwner(stage);
        setTitle(DIALOG_TITLE);
        setHeaderText(DIALOG_HEADER);
        setContentText(DIALOG_CONTENT);
        getButtonTypes().setAll(ButtonType.NO, ButtonType.YES);
    }
}
