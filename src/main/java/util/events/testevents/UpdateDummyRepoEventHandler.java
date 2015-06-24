package util.events.testevents;

import com.google.common.eventbus.Subscribe;
import util.events.EventHandler;

@FunctionalInterface
public interface UpdateDummyRepoEventHandler extends EventHandler {
    @Subscribe
    void handle(UpdateDummyRepoEvent e);
}
