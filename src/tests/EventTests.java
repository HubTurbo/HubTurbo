package tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import backend.assumed.ModelUpdatedEvent;
import backend.assumed.ModelUpdatedEventHandler;
import backend.resource.Model;
import org.eclipse.egit.github.core.RepositoryId;
import org.junit.Test;

import util.events.EventHandler;
import util.events.IssueSelectedEvent;
import util.events.IssueSelectedEventHandler;

import com.google.common.eventbus.EventBus;

import java.util.ArrayList;

public class EventTests {

    private EventBus events = new EventBus();

    @Test
    public void basics() {
        // Ensure that the right handler is triggered
        events.register(fail2);
        events.register(succeed1);
        
        ModelUpdatedEvent te = new ModelUpdatedEvent(new Model(RepositoryId.createFromId("test/test"), new ArrayList<>(),
	        new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
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

    private final EventHandler succeed2 = (IssueSelectedEventHandler) e -> assertTrue(true);
    private final EventHandler fail2 = (IssueSelectedEventHandler) e -> fail("IssueSelectedEventHandler failed");
    private final EventHandler succeed1 = (ModelUpdatedEventHandler) e -> assertTrue(true);
    private final EventHandler fail1 = (ModelUpdatedEventHandler) e -> fail("RefreshDoneEventHandler failed");
}
