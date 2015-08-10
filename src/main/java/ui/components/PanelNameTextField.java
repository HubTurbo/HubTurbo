package ui.components;

import ui.issuepanel.FilterPanel;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

public class PanelNameTextField extends TextField {

    private final FilterPanel panel;
    private final int PANEL_MAX_NAME_LENGTH = 48;
    private String previousText = "";
    private int caretPosition;

    public PanelNameTextField(String panelName, FilterPanel panel) {
        this.panel = panel;
        previousText = panelName;
        Platform.runLater(() -> {
            requestFocus();
            selectAll();
        });
        setText(panelName);
        setup();
        setPrefColumnCount(30);
    }

    private void setup() {
        setOnKeyReleased(e -> {
            e.consume();
            caretPosition = getCaretPosition();
            if (getText().length() > PANEL_MAX_NAME_LENGTH) {
                int extraLettersCount = (getText().length() - previousText.length());
                setText(previousText);
                Platform.runLater(() -> {
                    positionCaret(caretPosition - extraLettersCount);
                });
            }
            previousText = getText();
            
            if (e.getCode() == KeyCode.ESCAPE) {
                panel.closeRenameTextField(this);
            } else if (e.getCode() == KeyCode.ENTER) {
                String newName = getText().trim();
                if (!newName.equals("")) {
                    panel.setPanelName(newName);
                }
                panel.closeRenameTextField(this);
            }
        });
    }
}
