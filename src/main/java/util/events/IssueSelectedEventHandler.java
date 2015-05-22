package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface IssueSelectedEventHandler extends EventHandler {
	@Subscribe
	public void handle(IssueSelectedEvent e);
}