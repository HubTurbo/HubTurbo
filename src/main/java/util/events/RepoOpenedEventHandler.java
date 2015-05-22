package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface RepoOpenedEventHandler extends EventHandler {
	@Subscribe
	public void handle(RepoOpenedEvent e);
}