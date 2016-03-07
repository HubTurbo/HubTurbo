package util.events;

import com.google.common.eventbus.Subscribe;

/**
 * The PrimaryRepoOpenedEventHandler is meant to handle the PrimaryRepoOpenedEvent
 */
@FunctionalInterface
public interface PrimaryRepoOpenedEventHandler extends EventHandler {
    @Subscribe
    void handle(PrimaryRepoOpenedEvent e);
}
