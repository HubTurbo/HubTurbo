package util;

import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import service.ServiceManager;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Extensions to JavaFX's Platform class.
 *
 * Contains utility methods for running code in various ways,
 * on or off (but close to) the JavaFX application thread.
 */
public class PlatformEx {

	private static final Logger logger = LogManager.getLogger(PlatformEx.class.getName());

	private static final ExecutorService delayExecutor = Executors.newSingleThreadExecutor();

	/**
	 * Similar to Platform.runLater, but with a small delay, so UI updates have time to propagate.
	 * @param action
	 */
	public static void runLaterDelayed(Runnable action) {
		delayExecutor.execute(() -> {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				assert false;
			}
			Platform.runLater(action);
		});
	}
}