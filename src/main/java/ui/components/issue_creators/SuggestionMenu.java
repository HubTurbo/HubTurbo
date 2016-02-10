package ui.components.issue_creators;

import java.util.List;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;

/**
 * Pop-up menu that appears with suggestions for auto-completion
 *
 */
public class SuggestionMenu extends ContextMenu {
    
    private final int maxEntries;

    public SuggestionMenu(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    /**
     * Load search result in menu 
     * @param searchResult
     */
    public void loadSuggestions(List<String> searchResult) {
        getItems().clear();
        searchResult.stream().sorted().limit(maxEntries).forEach(this::addMenuItem);
        
    }

    /**
     *  Wraps a label inside a menu item.
     */
    private void addMenuItem(String content) {
        CustomMenuItem item = new CustomMenuItem(new Label(content), true);
        getItems().add(item);
        
        assert getItems().size() <= maxEntries;
    }
}
