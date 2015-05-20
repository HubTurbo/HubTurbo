package tests.stubs;

import util.events.Event;
import util.events.EventDispatcher;
import util.events.EventHandler;

import com.google.common.eventbus.EventBus;

public class ModelEventDispatcherStub implements EventDispatcher {

	private final EventBus events;

	public ModelEventDispatcherStub(EventBus eventBus) {
		this.events = eventBus;
	}

	@Override
	public void registerEvent(EventHandler handler) {
		events.register(handler);
	}

	@Override
	public <T extends Event> void triggerEvent(T event) {
		events.post(event);
	}

}
