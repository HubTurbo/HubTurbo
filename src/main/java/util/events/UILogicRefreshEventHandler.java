package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface UILogicRefreshEventHandler extends EventHandler {
    @Subscribe
    void handle(UILogicRefreshEvent e);
}
