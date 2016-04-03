package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import util.Utility;

import static ui.components.KeyboardShortcuts.REMOVE_FOCUS;

import java.util.List;
import java.util.function.Consumer;

public class RepositorySelector extends HBox {

    private final ComboBox<String> comboBox = new ComboBox<>();
    private final UI ui;
    private Consumer<String> onValueChangeCallback = e -> {
    };
    private boolean changesDisabled = false;

    public RepositorySelector(UI ui) {
        this.ui = ui;
        setupLayout();
        setupComboBox();
        getChildren().addAll(comboBox);
        comboBox.setId(IdGenerator.getRepositorySelectorId());
    }

    private void setupLayout() {
        setSpacing(5);
        setPadding(new Insets(5));
        setAlignment(Pos.BASELINE_LEFT);
        comboBox.setPrefWidth(250);
    }

    private void setupComboBox() {
        comboBox.setFocusTraversable(false);
        comboBox.setEditable(true);
        comboBox.valueProperty().addListener((observable, old, newVal) -> {
            if (newVal == null) {
                return;
            }

            String repoId = Utility.removeAllWhitespace(newVal);
            if (!repoId.equals(newVal)) {
                comboBox.setValue(repoId);
                return;
            }

            if (Utility.isWellFormedRepoId(repoId) && !changesDisabled) {
                onValueChangeCallback.accept(repoId);
            }
        });

        comboBox.addEventFilter(KeyEvent.KEY_PRESSED, event -> {

            //remove focus from the repo selector
            if (REMOVE_FOCUS.match(event)) {
                event.consume();
                getParent().requestFocus();
            }
        });

    }

    public void setOnValueChange(Consumer<String> callback) {
        assert callback != null;
        onValueChangeCallback = callback;
    }

    private void loadContents() {
        comboBox.getItems().addAll(ui.logic.getStoredRepos());
        comboBox.getItems().sort(String.CASE_INSENSITIVE_ORDER);
    }

    public List<String> getContents() {
        return comboBox.getItems();
    }

    public String getText() {
        return comboBox.getValue();
    }

    public void setText(String repoId) {
        changesDisabled = true;
        comboBox.setValue(repoId);
        changesDisabled = false;
        comboBox.getEditor().positionCaret(repoId.length());
    }

    public void refreshContents() {
        String text = getText();

        comboBox.getItems().clear();
        loadContents();

        setText(text);
    }

}
