package ui.components.issue_creators;

import java.util.List;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.stage.WindowEvent;

/**
 * Pop-up menu that appears with suggestions for auto-completion
 *
 */
public class SuggestionMenu extends ContextMenu {
    
    private final int maxEntries;

    // Selected menu item's content
    private Optional<String> selected = Optional.empty();

    public SuggestionMenu(int maxEntries) {
        this.maxEntries = maxEntries;
        setOnShowing(this::onShowingHandler);
    }

    /**
     * Load search result in menu 
     * @param searchResult
     */
    public void loadSuggestions(List<String> searchResult) {
        getItems().clear();

        searchResult.stream().limit(maxEntries).forEach(this::addMenuItem);
    }

    /**
     * Get content of selected menu item
     */
    public Optional<String> getSelectedContent() {
        return selected;
    }
    
    /**
     *  Wraps a label inside a menu item.
     */
    private void addMenuItem(String content) {
        CustomMenuItem item = new CustomMenuItem(new Label(content), false);
        item.setText(content);
        getItems().add(item);
        item.setOnAction(this::onActionHandler);
        assert getItems().size() <= maxEntries;
    }
    
    private void onShowingHandler(WindowEvent event) {
    }

    private void onActionHandler(ActionEvent event) {
        selected = Optional.of(((CustomMenuItem) event.getSource()).getText());
        hide();
    }
}
