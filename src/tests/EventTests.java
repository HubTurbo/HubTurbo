package tests;

import com.google.common.eventbus.EventBus;
import org.junit.Test;
import util.events.*;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class EventTests {

    private EventBus events = new EventBus();

    @Test
    public void basics() {
        // Ensure that the right handler is triggered
        events.register(fail2);
        events.register(succeed1);
        
        BoardSavedEvent te = new BoardSavedEvent();
        IssueSelectedEvent te2 = new IssueSelectedEvent("", 1, 2);

        events.post(te);

        // Remove handlers before next test
        events.unregister(fail2);
        events.unregister(succeed1);

        // Attempts to unregister handlers which aren't registered to begin with
        try {
            events.unregister(fail1);
            fail("Cannot unregister event handler which hasn't been registered");
        } catch (IllegalArgumentException e) {
        }
        try {
            events.unregister(succeed2);
            fail("Cannot unregister event handler which hasn't been registered");
        } catch (IllegalArgumentException e) {
        }

        // Try the other event
        events.register(fail1);
        events.register(succeed2);
        
        events.post(te2);
        
        events.unregister(fail1);
        events.unregister(succeed2);

        // Try both
        events.register(succeed1);
        events.register(succeed2);
        
        events.post(te);
        events.post(te2);
    }

    private final EventHandler succeed2 = (IssueSelectedEventHandler) e -> assertTrue(true);
    private final EventHandler fail2 = (IssueSelectedEventHandler) e -> fail("IssueSelectedEventHandler failed");
    private final EventHandler succeed1 = (BoardSavedEventHandler) e -> assertTrue(true);
    private final EventHandler fail1 = (BoardSavedEventHandler) e -> fail("BoardSavedEventHandler failed");
}
