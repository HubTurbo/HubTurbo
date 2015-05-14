package backend.assumed;

import com.google.common.eventbus.Subscribe;
import util.events.EventHandler;

@FunctionalInterface
public interface ModelUpdatedEventHandler extends EventHandler {
	@Subscribe
	public void handle(ModelUpdatedEvent e);
}