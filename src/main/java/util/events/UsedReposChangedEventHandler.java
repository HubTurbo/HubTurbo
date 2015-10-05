package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface UsedReposChangedEventHandler extends EventHandler {
    @Subscribe
    void handle(UsedReposChangedEvent e);
}
