package ui.components;

import java.util.List;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
    
    // Callback functions
    private EventHandler<ActionEvent> actionHandler = (event) -> defaultActionHandler(event);

    public SuggestionMenu(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    /**
     * Load search result in menu 
     * @param searchResult
     */
    public void loadSuggestions(List<String> searchResult) {
        getItems().clear();
        searchResult.stream().limit(maxEntries).forEach(this::addMenuItem);
        selected = searchResult.stream().findFirst();
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
        item.setOnAction(this.actionHandler);
        assert getItems().size() <= maxEntries;
    }
    
    /**
     * Sets selected content to event's source and hide the menu
     * @param event
     */
    private void defaultActionHandler(ActionEvent event) {
        selected = getTextOnAction(event);
        hide();
    }

    /**
     * @param event
     * @return text content from an event's source
     */
    public static Optional<String> getTextOnAction(ActionEvent event) {
        return Optional.of(((CustomMenuItem) event.getSource()).getText());
    }

    public SuggestionMenu setActionHandler(EventHandler<ActionEvent> actionHandler) {
        this.actionHandler = actionHandler;
        return this;
    }
}
