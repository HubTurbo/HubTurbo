package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface UpdateProgressEventHandler extends EventHandler {
	@Subscribe
	public void handle(UpdateProgressEvent e);
}