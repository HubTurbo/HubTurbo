package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface ColumnChangeEventHandler extends EventHandler {
	@Subscribe
	public void handle(ColumnChangeEvent e);
}