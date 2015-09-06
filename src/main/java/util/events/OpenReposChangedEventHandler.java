package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface OpenReposChangedEventHandler extends EventHandler {
    @Subscribe
    void handle(OpenReposChangedEvent e);
}
