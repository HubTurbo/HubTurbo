package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface RepoOpeningEventHandler extends EventHandler {
    @Subscribe
    void handle(RepoOpeningEvent e);
}
