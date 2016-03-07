package util.events;

import com.google.common.eventbus.Subscribe;

/**
 * The PrimaryRepoOpeningEventHandler is meant to handle the PrimaryRepoOpeningEvent
 */
@FunctionalInterface
public interface PrimaryRepoOpeningEventHandler extends EventHandler {
    @Subscribe
    void handle(PrimaryRepoOpeningEvent e);
}
