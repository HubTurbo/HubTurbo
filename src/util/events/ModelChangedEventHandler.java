package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface ModelChangedEventHandler extends EventHandler {
	@Subscribe
	public void handle(ModelChangedEvent e);
}