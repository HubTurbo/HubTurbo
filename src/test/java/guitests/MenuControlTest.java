package guitests;

import static org.junit.Assert.assertEquals;
import static ui.components.KeyboardShortcuts.REFRESH;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import ui.UI;
import util.PlatformEx;
import util.events.ModelUpdatedEventHandler;

public class MenuControlTest extends UITest {

    private final AtomicInteger triggered = new AtomicInteger(0);

    @Before
    public void setupUIComponent() {
        PlatformEx.runAndWait(() ->
                UI.events.registerEvent((ModelUpdatedEventHandler) e -> triggered.incrementAndGet()));
    }

    @Test
    public void refresh_refreshCount_refreshTriggersCorrectEvent() {
        push(REFRESH);
        assertEquals(2, triggered.get());
    }
}
