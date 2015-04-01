package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface ColumnClickedEventHandler extends EventHandler {
	@Subscribe
	public void handle(ColumnClickedEvent e);
}