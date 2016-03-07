package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface FilterRepoOpenedEventHandler extends EventHandler {
    @Subscribe
    void handle(FilterRepoOpenedEvent e);
}
