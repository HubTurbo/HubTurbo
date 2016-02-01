package util.events;

import com.google.common.eventbus.Subscribe;

/**
 * The RepoOpenedEventHandler is meant to handle the RepoOpenedEvent
 */
@FunctionalInterface
public interface RepoOpenedEventHandler extends EventHandler {
    @Subscribe
    void handle(RepoOpenedEvent e);
}
