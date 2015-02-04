package tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import util.events.EventHandler;
import util.events.IssueSelectedEvent;
import util.events.IssueSelectedEventHandler;
import util.events.ModelChangedEvent;
import util.events.ModelChangedEventHandler;

import com.google.common.eventbus.EventBus;

public class EventTests {

    private EventBus events = new EventBus();

    @Test
    public void basics() {
        // Ensure that the right handler is triggered
        events.register(fail2);
        events.register(succeed1);
        
        ModelChangedEvent te = new ModelChangedEvent(null, null, null, null);
        IssueSelectedEvent te2 = new IssueSelectedEvent(1, 2);

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

    private final EventHandler succeed2 = new IssueSelectedEventHandler() {
        @Override
        public void handle(IssueSelectedEvent e) {
            assertTrue(true);
        }
    };
    private final EventHandler fail2 = new IssueSelectedEventHandler() {
        @Override
        public void handle(IssueSelectedEvent e) {
            fail("IssueSelectedEventHandler failed");
        }
    };
    private final EventHandler succeed1 = new ModelChangedEventHandler() {
        @Override
        public void handle(ModelChangedEvent e) {
            assertTrue(true);
        }
    };
    private final EventHandler fail1 = new ModelChangedEventHandler() {
        @Override
        public void handle(ModelChangedEvent e) {
            fail("RefreshDoneEventHandler failed");
        }
    };
}
