package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface IssueViewedEventHandler extends EventHandler {
	@Subscribe
	public void handle(IssueViewedEvent e);
}