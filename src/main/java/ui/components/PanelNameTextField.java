package ui.components;

import ui.issuepanel.FilterPanel;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

public class PanelNameTextField extends TextField {

    private final FilterPanel panel;
    private final int PANEL_MAX_NAME_LENGTH = 48;
    private String previousText = "";
    private int caretPosition = 0;

    public PanelNameTextField(String panelName, FilterPanel panel) {
        this.panel = panel;
        previousText = panelName;
        Platform.runLater(() -> {
            requestFocus();
            selectAll();
        });
        setup();
        setPrefColumnCount(30);
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
            
            if (e.getCode() == KeyCode.ESCAPE) {
                panel.closeRenameTextField(this);
            }
        });
    }
}
