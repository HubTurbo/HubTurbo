package ui.components;

import javafx.application.Platform;
import javafx.scene.control.TextField;

public class PanelNameTextField extends TextField {

    private final int PANEL_MAX_NAME_LENGTH = 48;
    private String previousText = "";
    private int caretPosition = 0;

    public PanelNameTextField(String panelName) {
        previousText = panelName;
        Platform.runLater(() -> {
            requestFocus();
        });
        setup();
        setText(panelName);
    }

    private void setup() {
        setOnKeyReleased(e -> {
            e.consume();
            caretPosition = getCaretPosition();
            if (getText().length() > PANEL_MAX_NAME_LENGTH) {
                setText(previousText);
                positionCaret(caretPosition - 1);
            }
            previousText = getText();
        });
    }
}
