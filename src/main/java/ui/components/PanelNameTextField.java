package ui.components;

import ui.issuepanel.FilterPanel;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

public class PanelNameTextField extends TextField {

    private final FilterPanel panel;
    private String previousText = "";
    private static final int PANEL_MAX_NAME_LENGTH = 36;

    public PanelNameTextField(String panelName, FilterPanel panel) {
        this.panel = panel;
        previousText = panelName;
        Platform.runLater(() -> {
            requestFocus();
            selectAll();
        });
        setText(panelName);
        setup();
        textProperty().addListener(c -> {
            // Prevent excessive characters
            if (getText().length() > PANEL_MAX_NAME_LENGTH) {
                setText(previousText);
            }
            previousText = getText();
        });
        setPrefColumnCount(30);
    }

    private void setup() {
        setOnKeyReleased(e -> {
            
            if (e.getCode() == KeyCode.ESCAPE) {
                panel.closeRenameTextField(this);
            } else if (e.getCode() == KeyCode.ENTER) {
                String newName = getText().trim();
                if (!newName.equals("")) {
                    panel.setPanelName(newName);
                }
                panel.closeRenameTextField(this);
            }

            e.consume();
        });
    }
}
