package util.events;

import com.google.common.eventbus.EventBus;

/**
 * Stub class used for headless firing of events.
 * Used for testing.
 */
public class EventDispatcherStub implements EventDispatcher {
	EventBus eventBus;

	public EventDispatcherStub() {
		this.eventBus = new EventBus();
	}

	public void registerEvent(EventHandler handler) {
		eventBus.register(handler);
	}

	public void unregisterEvent(EventHandler handler) {
		eventBus.unregister(handler);
	}

	public <T extends Event> void triggerEvent(T event) {
		eventBus.post(event);
	}

}
