package util;

import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Extensions to JavaFX's Platform class.
 *
 * Contains utility methods for running code in various ways,
 * on or off (but close to) the JavaFX application thread.
 */
public final class PlatformEx {

    private static final ExecutorService delayExecutor = Executors.newSingleThreadExecutor();

    /**
     * Similar to Platform.runLater, but with a small delay, so UI updates have time to propagate.
     * @param action
     */
    public static void runLaterDelayed(Runnable action) {
        runLaterDelayed(action, 300);
    }

    public static void runLaterDelayed(Runnable action, int delay) {
        delayExecutor.execute(() -> {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                assert false;
            }
            Platform.runLater(action);
        });
    }

    /**
     * Blocks until the JavaFX event queue becomes empty.
     */
    public static void waitOnFxThread() {
        runLaterAndWait(() -> {});
    }

    /**
     * Runs an action on the JavaFX Application Thread and blocks until it completes.
     * Similar to {@link #runAndWait(Runnable) runAndWait}, but always enqueues the
     * action, eschewing checking the current thread.
     * @param action The action to run on the JavaFX Application Thread
     */
    public static void runLaterAndWait(Runnable action) {
        assert action != null : "Non-null action required";
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            action.run();
            latch.countDown();
        });
        try {
            latch.await();
        } catch (InterruptedException ignored) {}
    }

    /**
     * Synchronous version of Platform.runLater, like SwingUtilities.invokeAndWait.
     * Caveat: will execute immediately when invoked from the JavaFX application thread
     * instead of being queued up for execution.
     * @param action The action to execute on the JavaFX Application Thread.
     */
    public static void runAndWait(Runnable action) {
        assert action != null : "Non-null action required";
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }
        runLaterAndWait(action);
    }

    private PlatformEx() {}
}
