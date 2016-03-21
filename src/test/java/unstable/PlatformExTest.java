package unstable;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.testfx.api.FxToolkit;

import guitests.UITest;
import javafx.application.Platform;
import util.PlatformEx;

public class PlatformExTest extends UITest {

    @Override
    public void setup() throws TimeoutException {
        FxToolkit.setupApplication(TestUI.class);
    }

    /**
     * This method is needed to sequence concurrent tests. Order matters for these
     * as they rely on Platform.runLater, so we can't let them run in arbitrary order.
     */
    @Test
    public void sequenceTests() {
        waitOnFxThreadTest();
        runLaterAndWaitTest();
    }

    public void waitOnFxThreadTest() {
        AtomicInteger result = new AtomicInteger(0);
        Platform.runLater(result::incrementAndGet);
        PlatformEx.waitOnFxThread();
        assertEquals(1, result.get());
    }

    public void runLaterAndWaitTest() {
        // On another thread, runLaterAndWait blocks until the operation is done
        AtomicInteger result = new AtomicInteger(0);
        PlatformEx.runLaterAndWait(result::incrementAndGet);
        assertEquals(1, result.get());

        // On the UI thread, the callback doesn't happen immediately
        Platform.runLater(() -> {
            assertEquals(1, result.get());
            PlatformEx.runLaterAndWait(result::incrementAndGet);
            assertEquals(1, result.get());
        });
    }

    @Test
    public void runAndWaitTest() {
        // On another thread, runAndWait blocks until the operation is done
        AtomicInteger result = new AtomicInteger(0);
        PlatformEx.runAndWait(result::incrementAndGet);
        assertEquals(1, result.get());

        // On the UI thread, the callback is invoked immediately
        Platform.runLater(() -> {
            assertEquals(1, result.get());
            PlatformEx.runAndWait(result::incrementAndGet);
            assertEquals(2, result.get());
        });
    }
}
