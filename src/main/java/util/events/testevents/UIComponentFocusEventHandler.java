package util.events.testevents;

import com.google.common.eventbus.Subscribe;
import util.events.EventHandler;

@FunctionalInterface
public interface UIComponentFocusEventHandler extends EventHandler {
    @Subscribe
    void handle(UIComponentFocusEvent e);
}
