package ui.components;

import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static ui.components.KeyboardShortcuts.FIRST_ISSUE;
import static ui.components.KeyboardShortcuts.LAST_ISSUE;

import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * A very specialized ListView subclass that:
 * <p>
 * - can be navigated with the arrow keys and Enter
 * - supports an event for item selection
 * - provides methods for retaining selection of an item (not by index)
 * after its contents are changed
 * <p>
 * It depends on the functionality of ScrollableListView to ensure that
 * navigation scrolls the list properly. The Up, Down, and Enter key events
 * should not be separately bound on it.
 * <p>
 * An item is considered selected when:
 * <p>
 * - it is highlighted with the arrow keys, but only when the Shift key is not down
 * - Enter is pressed when it is highlighted
 * - it is clicked
 *
 * @param <T> The type of the item in the list
 */
public abstract class NavigableListView<T> extends ScrollableListView<T> {

    private static final Logger logger = LogManager.getLogger(NavigableListView.class.getName());

    // Tracks the index of the item in the list which should be currently selected
    protected Optional<Integer> selectedIndex = Optional.empty();

    // Used for saving and restoring selection by item.
    // selectedIndex should be used to get the currently-selected item, through the provided getter.
    private Optional<T> lastSelectedItem = Optional.empty();

    // Indicates that saveSelection was called, in the event that saveSelection itself fails
    // (when nothing is selected, both should be no-ops)
    private boolean saveSelectionCalled = false;

    public static final boolean IS_RIGHT_CLICK = true;

    private BiConsumer<Integer, Boolean> onItemSelected = (index, rightKey) -> {
    };

    public NavigableListView() {
        setupKeyEvents();
        setupMouseEvents();
    }

    /**
     * Should be called before making changes to the item list of this list view if
     * it's important that selection is retained after.
     */
    public void saveSelection() {
        if (getSelectionModel().getSelectedItem() != null) {
            lastSelectedItem = Optional.of(getSelectionModel().getSelectedItem());
        }
        saveSelectionCalled = true;
    }

    /**
     * Should be called to restore selection after making changes to the item list
     * of this list view. Must be called after saveSelection is.
     *
     * @throws IllegalStateException if called before saveSelection is
     */
    public void restoreSelection() {
        if (!lastSelectedItem.isPresent()) {
            if (!saveSelectionCalled) {
                throw new IllegalStateException("saveSelection must be called before restoreSelection");
            } else {
                saveSelectionCalled = false;
                return; // No-op
            }
        }
        saveSelectionCalled = false;

        // Find index of previously-selected item
        int index = -1;
        int i = 0;
        for (T item : getItems()) {
            if (areItemsEqual(item, lastSelectedItem.get())) {
                index = i;
                break;
            }
            i++;
        }
        boolean itemFound = index > -1;

        if (itemFound) {
            // Select that item
            getSelectionModel().clearAndSelect(index);
            selectedIndex = Optional.of(index);
            // Do not trigger event; selection did not conceptually change
        } else {
            // The item disappeared
            if (getItems().size() == 0) {
                // No more items in the list
                selectedIndex = Optional.empty();
            } else {
                // The list is non-empty, so we can be sure that we're selecting something
                // The current index is the same as the next, due to the item disappearing
                int lastIndex = getItems().size() - 1;
                int nextIndex = Math.min(selectedIndex.get(), lastIndex);

                getSelectionModel().clearAndSelect(nextIndex);
                selectedIndex = Optional.of(nextIndex);

                // The next item will be considered selected
                triggerItemSelected(nextIndex);
            }
        }
    }

    /**
     * Triggers the selected item on the card at the specified index for actions that are not right clicks
     */
    private void triggerItemSelected(int index) {
        onItemSelected.accept(index, !IS_RIGHT_CLICK);
    }

    /**
     * Triggers the selected item on the card at the specified index for actions that are right clicks
     */
    private void triggerItemSelectedRightClick(int index) {
        onItemSelected.accept(index, IS_RIGHT_CLICK);
    }

    public abstract boolean areItemsEqual(T item1, T item2);

    private void setupMouseEvents() {
        setOnMouseClicked(e -> {
            int currentlySelected = getSelectionModel().getSelectedIndex();
            // The currently-selected index is sometimes -1 when an issue is clicked.
            // When this happens we ignore this event.
            if (currentlySelected != -1) {
                selectedIndex = Optional.of(currentlySelected);

                logger.info("Mouse click on item index " + selectedIndex.get());
                if (e.getButton().equals(MouseButton.SECONDARY)) {
                    triggerItemSelectedRightClick(currentlySelected);
                } else {
                    triggerItemSelected(currentlySelected);
                }
            }
        });
    }

    private void setupKeyEvents() {
        setOnKeyPressed(e -> {
            if (e.isControlDown()) {
                return;
            }
            if (e.getCode() == KeyCode.ENTER) {
                e.consume();
                if (selectedIndex.isPresent()) {
                    logger.info("Enter key selection on item " + selectedIndex.get());
                    triggerItemSelected(selectedIndex.get());
                }
            }
            if (e.getCode() == KeyCode.UP || e.getCode() == KeyCode.DOWN ||
                    KeyboardShortcuts.upIssue.match(e) || KeyboardShortcuts.downIssue.match(e)) {
                e.consume();
                handleUpDownKeys(e.getCode() == KeyCode.DOWN || KeyboardShortcuts.downIssue.match(e));
                assert selectedIndex.isPresent() : "handleUpDownKeys doesn't set selectedIndex!";
                if (!e.isShiftDown()) {
                    logger.info("Enter key selection on item index " + selectedIndex.get());
                    triggerItemSelected(selectedIndex.get());
                }
            }
            if (FIRST_ISSUE.match(e)) {
                e.consume();
                selectNthItem(1);
            }
            if (LAST_ISSUE.match(e)) {
                e.consume();
                selectNthItem(getItems().size());
            }
        });
    }

    public void handleUpDownKeys(boolean isDownKey) {
        // Nothing is selected or the list is empty; do nothing
        if (!selectedIndex.isPresent()) return;
        if (getItems().size() == 0) return;

        // Compute new index and clamp it within range
        int newIndex = selectedIndex.get() + (isDownKey ? 1 : -1);
        newIndex = Math.min(Math.max(0, newIndex), getItems().size() - 1);

        // Update selection state and our selection model
        getSelectionModel().clearAndSelect(newIndex);
        selectedIndex = Optional.of(newIndex);

        // Ensure that the newly-selected item is in view
        scrollAndShow(newIndex);
    }

    public void setOnItemSelected(BiConsumer<Integer, Boolean> callback) {
        onItemSelected = callback;
    }

    /**
     * Selects the nth item in the navigable list.
     *
     * @param n - Nth item, indexing is 1-based (1st item would correspond to n = 1)
     */

    public final void selectNthItem(int n) {
        requestFocus();
        assert n <= getItems().size() && n > 0;
        getSelectionModel().clearAndSelect(n - 1);
        scrollAndShow(n - 1);
        selectedIndex = Optional.of(n - 1);
        triggerItemSelected(selectedIndex.get());
    }

    public void selectNextItem() {
        requestFocus();
        if (selectedIndex.get() < getItems().size() - 1) {
            getSelectionModel().clearAndSelect(selectedIndex.get() + 1);
            scrollAndShow(selectedIndex.get() + 1);
            selectedIndex = Optional.of(selectedIndex.get() + 1);
            triggerItemSelected(selectedIndex.get());
        }
    }

    public Optional<T> getSelectedItem() {
        return selectedIndex.map(getItems()::get);
    }

    /**
     * @return The selected index in the list panel
     */
    public Optional<Integer> getSelectedIndex() {
        return selectedIndex;
    }

}
