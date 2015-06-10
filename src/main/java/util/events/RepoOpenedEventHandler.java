package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface RepoOpenedEventHandler extends EventHandler {
    @Subscribe
    void handle(RepoOpenedEvent e);
}
