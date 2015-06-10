package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface ModelUpdatedEventHandler extends EventHandler {
    @Subscribe
    void handle(ModelUpdatedEvent e);
}
