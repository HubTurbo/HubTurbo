package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface LabelCreatedEventHandler extends EventHandler {
	@Subscribe
	public void handle(LabelCreatedEvent e);
}