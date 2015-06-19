package util;

import javafx.scene.control.Alert;
import org.controlsfx.dialog.Dialogs;

import javafx.concurrent.Task;

public class DialogMessage {
    public static void showErrorDialog(String title, String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
