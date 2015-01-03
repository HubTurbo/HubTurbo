package ui;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;

import com.sun.javafx.scene.control.skin.VirtualFlow;

public class ScrollingListView<T> extends ListView<T> {

	public static final int Y_OFFSET = 10;
	public static final int SCROLL_THREAD_SLEEP_INTERVAL = 15;
	
	public static final int DIRECTION_NONE = 0;
	public static final int DIRECTION_UP = -1;
	public static final int DIRECTION_DOWN = 1;
	
	private Optional<Integer> selectedIndex = Optional.empty();
	private Executor executor = Executors.newSingleThreadExecutor();;
	
	private IntConsumer onItemSelected = i -> {};
	
	public ScrollingListView() {
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
	
	/**
	 * Scrolls until the item with the given index is visible.
	 * @param newIndex
	 */
	public void scrollAndShow(int newIndex) {
		final VirtualFlow<?> flow = getVirtualFlow();
		BooleanSupplier condition = () -> {
			if (flow.getFirstVisibleCellWithinViewPort() == null
					|| flow.getLastVisibleCellWithinViewPort() == null) {
				// These are null for an unknown reason; stop scrolling
				return false;
			}
			
			int firstIndex = flow.getFirstVisibleCellWithinViewPort().getIndex();
			int lastIndex = flow.getLastVisibleCellWithinViewPort().getIndex();
			
			if (newIndex >= firstIndex && newIndex <= lastIndex) {
				// The index we want is visible; we're done
				return false;
			} else if (newIndex >= firstIndex) {
				// Scroll down
				return flow.getLastVisibleCellWithinViewPort().getIndex() != newIndex;
			} else {
				assert newIndex <= lastIndex : "Missing a case";
				// Scroll up
				return flow.getFirstVisibleCellWithinViewPort().getIndex() != newIndex;
			}
		};
		
		IntSupplier direction = () -> {
			if (flow.getFirstVisibleCellWithinViewPort() == null
					|| flow.getLastVisibleCellWithinViewPort() == null) {
				// These are null for an unknown reason; no direction to indicate
				// here. The condition predicate should stop this in any case.
				return DIRECTION_NONE; // whatever
			}
			
			int firstIndex = flow.getFirstVisibleCellWithinViewPort().getIndex();
			int lastIndex = flow.getLastVisibleCellWithinViewPort().getIndex();
			
			if (newIndex >= firstIndex && newIndex <= lastIndex) {
				// The index we want is visible; we're done
				return DIRECTION_NONE;
			} else if (newIndex >= firstIndex) {
				return DIRECTION_DOWN;
			} else {
				assert newIndex <= lastIndex : "Missing a case";
				return DIRECTION_UP;
			}
		};
		
		executor.execute(new ScrollThread(condition, direction));
	}

	private VirtualFlow<?> getVirtualFlow() {
		return (VirtualFlow<?>) lookup("VirtualFlow");
	}

	class ScrollThread extends Thread {
		
		private BooleanSupplier continueRunning;
		private IntSupplier direction;
		private VirtualFlow<?> flow;
		
		public ScrollThread(BooleanSupplier continueRunning, IntSupplier direction) {
			super("ScrollThread");
			setDaemon(true);

			assert continueRunning != null;
			assert direction != null;

			this.continueRunning = continueRunning;
			this.direction = direction;
			this.flow = getVirtualFlow();
		}
		
		@Override
		public void run() {

			while (continueRunning.getAsBoolean()) {

				Platform.runLater(() -> {
					scrollY();
				});

				try {
					sleep(SCROLL_THREAD_SLEEP_INTERVAL);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		private void scrollY() {
			flow.adjustPixels(direction.getAsInt() * Y_OFFSET);
		}

	}
}