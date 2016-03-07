package ui.listpanel;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class ConfirmCloseReopenDialog extends Alert {

    private static final String DIALOG_TITLE = "%s Issue?";
    private static final String DIALOG_HEADER = "This will be %s on Github.";
    private static final String DIALOG_CONTENT = "Are you sure?";

    public ConfirmCloseReopenDialog(Stage stage, boolean open) {
        super(AlertType.CONFIRMATION);
        initOwner(stage);
        setTitle(String.format(DIALOG_TITLE, open ? "Reopen" : "Close"));
        setHeaderText(String.format(DIALOG_HEADER, open ? "reopened" : "closed"));
        setContentText(DIALOG_CONTENT);
    }
}
