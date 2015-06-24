package util.events.testevents;

import com.google.common.eventbus.Subscribe;
import util.events.EventHandler;

@FunctionalInterface
public interface NavigateToPageEventHandler extends EventHandler {
    @Subscribe
    void handle(NavigateToPageEvent e);
}
