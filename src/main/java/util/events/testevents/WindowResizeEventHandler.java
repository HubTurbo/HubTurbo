package util.events.testevents;

import com.google.common.eventbus.Subscribe;
import util.events.EventHandler;

@FunctionalInterface
public interface WindowResizeEventHandler extends EventHandler {
    @Subscribe
    void handle(WindowResizeEvent e);
}
