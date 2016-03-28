package unstable;

import static ui.components.KeyboardShortcuts.REFRESH;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import guitests.UITest;
import ui.UI;
import util.PlatformEx;
import util.events.ModelUpdatedEventHandler;

public class MenuControlTest extends UITest {
    @Test
    public void refresh_refreshCount_refreshTriggersCorrectEvent() {
        final AtomicInteger triggered = new AtomicInteger(0);

        PlatformEx.runAndWait(() ->
                UI.events.registerEvent((ModelUpdatedEventHandler) e -> triggered.incrementAndGet()));

        press(REFRESH);
        waitAndAssertEquals(1, triggered::get);
    }
}
