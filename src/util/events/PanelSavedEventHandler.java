package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface PanelSavedEventHandler extends EventHandler {
	@Subscribe
	public void handle(PanelSavedEvent e);
}