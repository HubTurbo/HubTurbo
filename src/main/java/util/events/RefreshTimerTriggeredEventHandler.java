package util.events;

import com.google.common.eventbus.Subscribe;

/**
 * Handles the RefreshTimerTriggeredEvent event.
 */
@FunctionalInterface
public interface RefreshTimerTriggeredEventHandler extends EventHandler {
    @Subscribe
    void handle(RefreshTimerTriggeredEvent e);
}
