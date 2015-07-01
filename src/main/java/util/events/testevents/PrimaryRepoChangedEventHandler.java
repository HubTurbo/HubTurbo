package util.events.testevents;

import com.google.common.eventbus.Subscribe;
import util.events.EventHandler;

@FunctionalInterface
public interface PrimaryRepoChangedEventHandler extends EventHandler {
    @Subscribe
    void handle(PrimaryRepoChangedEvent e);
}
