package ui.components;

import java.util.List;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;

/**
 * Pop-up menu that appears with suggestions for auto-completion
 *
 */
public class SuggestionsMenu extends ContextMenu {
    
    private final int maxEntries;

    // Selected menu item's content
    private Optional<String> selected = Optional.empty();
    
    // Callback functions
    private EventHandler<ActionEvent> actionHandler = this::defaultActionHandler;

    public SuggestionsMenu(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    public void show(Node anchor) {
        this.show(anchor, Side.BOTTOM, 0, 0);
    }

    /**
     * Loads search result in menu 
     * @param searchResult
     */
    public void loadSuggestions(List<String> searchResult) {
        getItems().clear();
        searchResult.stream().limit(maxEntries).forEach(this::addMenuItem);
        selected = searchResult.stream().findFirst();
    }

    /**
     * Gets content of selected menu item
     */
    public Optional<String> getSelectedContent() {
        return selected;
    }
    
    /**
     * Adds a menuItem with content as its text
     * @param content
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

    public SuggestionsMenu setActionHandler(EventHandler<ActionEvent> actionHandler) {
        this.actionHandler = actionHandler;
        return this;
    }
}
