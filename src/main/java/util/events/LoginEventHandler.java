package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface LoginEventHandler extends EventHandler {
	@Subscribe
	public void handle(LoginEvent e);
}