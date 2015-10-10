package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface UnusedStoredReposChangedEventHandler extends EventHandler {
    @Subscribe
    void handle(UnusedStoredReposChangedEvent e);
}
