package util;

import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.concurrent.CountDownLatch;

public class PlatformEx {

    private static final Logger logger = LogManager.getLogger(PlatformEx.class.getName());

    /**
     * Synchronous version of Platform.runLater, like SwingUtilities.invokeAndWait.
     * Caveat: will execute immediately when invoked from the JavaFX application thread
     * instead of being queued up for execution.
     * @param action
     */
    public static void runAndWait(Runnable action) {
        if (action == null) {
            throw new NullPointerException("runAndWait cannot accept a null action");
        }

        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            action.run();
            latch.countDown();
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
