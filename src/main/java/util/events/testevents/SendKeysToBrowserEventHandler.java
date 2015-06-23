package util.events.testevents;

import com.google.common.eventbus.Subscribe;
import util.events.EventHandler;

@FunctionalInterface
public interface SendKeysToBrowserEventHandler extends EventHandler {
    @Subscribe
    void handle(SendKeysToBrowserEvent e);
}
