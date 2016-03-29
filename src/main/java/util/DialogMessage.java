package util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public final class DialogMessage {

    private DialogMessage() {
    }

    public static void showErrorDialog(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static boolean showYesNoWarningDialog(String title, String header, String message,
                                                 String yesButtonLabel, String noButtonLabel) {

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);

        ButtonType yesButton = new ButtonType(yesButtonLabel);
        ButtonType noButton = new ButtonType(noButtonLabel);
        alert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = alert.showAndWait();

        return result.get().equals(yesButton);
    }

    public static void showInformationDialog(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
