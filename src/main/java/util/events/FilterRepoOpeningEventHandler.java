package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface FilterRepoOpeningEventHandler extends EventHandler {
    @Subscribe
    void handle(FilterRepoOpeningEvent e);
}
