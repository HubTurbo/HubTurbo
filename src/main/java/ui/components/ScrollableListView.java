package ui.components;

import com.sun.javafx.scene.control.skin.VirtualFlow;
import javafx.application.Platform;
import javafx.scene.control.ListView;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

/**
 * A ListView subclass that can be programmatically scrolled.
 * <p>
 * It builds on the functionality of the vanilla ListView by adding
 * methods for scrolling in different ways and can be easily extended
 * to support other kinds of scrolling.
 *
 * @param <T> The type of the item in the list
 */
public class ScrollableListView<T> extends ListView<T> {

    // The distance in pixels that will be scrolled per interval.
    // This is a reasonable default for the general case. Can be increased
    // if the speed of scrolling is to be increased, but must not exceed the
    // height of the ListView item, or it may scroll right past the target
    // item and become unable to terminate.
    private static final int Y_OFFSET = 10;

    // The interval for scrolling in ms. Subject to system-specific granularity
    // so it should not be too low.
    private static final int SCROLL_THREAD_SLEEP_INTERVAL = 15;

    // Constants to indicate which direction to scroll. NONE is not meant to be
    // used and serves as a no-op in the event that it is actually used.
    private static final int DIRECTION_NONE = 0;
    private static final int DIRECTION_UP = -1;
    private static final int DIRECTION_DOWN = 1;

    // Scroll events are queued, and only one will be active at a time.
    private final Executor executor = Executors.newSingleThreadExecutor();

    /**
     * Scrolls until the item with the given index is visible.
     *
     * @param newIndex
     */
    public void scrollAndShow(int newIndex) {
        final VirtualFlow<?> flow = getVirtualFlow();
        if (flow == null) {
            return;
        }
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
                return DIRECTION_NONE;
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

    private class ScrollThread extends Thread {

        private final BooleanSupplier continueRunning;
        private final IntSupplier direction;
        private final VirtualFlow<?> flow;

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

                Platform.runLater(this::scrollY);

                try {
                    sleep(SCROLL_THREAD_SLEEP_INTERVAL);
                } catch (InterruptedException e) {
                    // Do nothing
                }
            }
        }

        private void scrollY() {
            flow.adjustPixels(direction.getAsInt() * Y_OFFSET);
        }

    }
}
