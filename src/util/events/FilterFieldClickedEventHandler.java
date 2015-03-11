package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface FilterFieldClickedEventHandler extends EventHandler {
	@Subscribe
	public void handle(FilterFieldClickedEvent e);
}