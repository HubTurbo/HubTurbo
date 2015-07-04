package util.events.testevents;

import com.google.common.eventbus.Subscribe;
import util.events.EventHandler;

@FunctionalInterface
public interface ClearLogicModelEventHandler extends EventHandler {
    @Subscribe
    void handle(ClearLogicModelEvent e);
}
