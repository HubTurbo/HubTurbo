package util.events;

import com.google.common.eventbus.Subscribe;

/**
 * The RepoOpeningEventHandler is meant to handle the RepoOpeningEvent
 */
@FunctionalInterface
public interface RepoOpeningEventHandler extends EventHandler {
    @Subscribe
    void handle(RepoOpeningEvent e);
}
