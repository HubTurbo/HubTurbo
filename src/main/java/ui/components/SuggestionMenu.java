package ui.components;

import java.util.List;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;

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
    }

    /**
     * Load search result in menu 
     * @param searchResult
     */
    public void loadSuggestions(List<String> searchResult) {
        getItems().clear();
        // Resets selection for every new trigger 
        selected = Optional.empty();
        searchResult.stream().limit(maxEntries).forEach(this::addMenuItem);

        // Sets focus on first item and select it as default suggestion
        if (isShowing() && !searchResult.isEmpty()) {
            getSkin().getNode().lookup(".menu-item").requestFocus();
            selected = Optional.ofNullable(searchResult.get(0));
        }
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
        Label label = new Label(content);
        CustomMenuItem item = new CustomMenuItem(label, false);
        item.setText(content);
        getItems().add(item);
        item.setOnAction(this::onActionHandler);
        assert getItems().size() <= maxEntries;
    }
    
    private void onActionHandler(ActionEvent event) {
        selected = Optional.of(((CustomMenuItem) event.getSource()).getText());
        hide();
    }
}
