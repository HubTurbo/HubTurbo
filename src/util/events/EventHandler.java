package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface EventHandler<T extends Event> {
	@Subscribe public void handle(T eventData);
}
