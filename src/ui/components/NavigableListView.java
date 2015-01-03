package ui.components;

import java.util.Optional;
import java.util.function.IntConsumer;

import javafx.scene.input.KeyCode;

/**
 * A very specialized ListView subclass that:
 * 
 * - can be navigated with the arrow keys and Enter
 * - supports an event for item selection
 * - correctly retains selection after its contents are cleared
 * 
 * It depends on the functionality of ScrollableListView to ensure that
 * navigation scrolls the list properly. The Up, Down, and Enter key events
 * should not be separately bound on it.
 * 
 * An item is considered selected when:
 * 
 * - it is highlighted with the arrow keys, but only when the Shift key is not down
 * - Enter is pressed when it is highlighted
 * - it is clicked
 */
public class NavigableListView<T> extends ScrollableListView<T> {

	private Optional<Integer> selectedIndex = Optional.empty();
	
	private IntConsumer onItemSelected = i -> {};
	
	public NavigableListView() {
		setupKeyEvents();
		setupMouseEvents();
	}

	private void setupMouseEvents() {
		setOnMouseClicked(e -> {
			selectedIndex = Optional.of(getSelectionModel().getSelectedIndex());
			onItemSelected.accept(selectedIndex.get());
		});
	}

	private void setupKeyEvents() {
		setOnKeyPressed(e -> {
			switch (e.getCode()) {
			case UP:
			case DOWN:
				e.consume();
				handleUpDownKeys(e.getCode() == KeyCode.DOWN);
				assert selectedIndex.isPresent() : "handleUpDownKeys doesn't set selectedIndex!";
				if (!e.isShiftDown()) {
					onItemSelected.accept(selectedIndex.get());
				}
				break;
			case ENTER:
				e.consume();
				if (selectedIndex.isPresent()) {
					onItemSelected.accept(selectedIndex.get());	
				}
				break;
			default:
				break;
			}
		});
	}
	
	private void handleUpDownKeys(boolean isDownKey) {
		
		// Nothing is selected or the list is empty; do nothing
		if (!selectedIndex.isPresent()) return;
		if (getItems().size() == 0) return;
		
		// Compute new index and clamp it within range
		int newIndex = selectedIndex.get() + (isDownKey ? 1 : -1);
		newIndex = Math.min(Math.max(0, newIndex), getItems().size()-1);
		
		// Update selection state and our selection model
		getSelectionModel().clearAndSelect(newIndex);
		selectedIndex = Optional.of(newIndex);
		
		// Ensure that the newly-selected item is in view
		scrollAndShow(newIndex);
	}

	public void setOnItemSelected(IntConsumer callback) {
		onItemSelected = callback;
	}
}