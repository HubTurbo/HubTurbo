package ui.listpanel;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * This class represents a popup dialog asking the user to confirm a Close / Reopen action on an issue.
 */
public class ConfirmCloseOrReopenIssueDialog extends Alert {

    private static final String DIALOG_TITLE = "%s Issue?";
    private static final String DIALOG_HEADER = "This will be %s on Github.";
    private static final String DIALOG_CONTENT = "Are you sure?";

    public ConfirmCloseOrReopenIssueDialog(Stage stage, boolean isOpen) {
        super(AlertType.CONFIRMATION);
        initOwner(stage);
        setTitle(String.format(DIALOG_TITLE, isOpen ? "Reopen" : "Close"));
        setHeaderText(String.format(DIALOG_HEADER, isOpen ? "reopened" : "closed"));
        setContentText(DIALOG_CONTENT);
    }
}
