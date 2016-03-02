package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface AppliedFilterEventHandler extends EventHandler {
    @Subscribe
    void handle(AppliedFilterEvent e);
}
