package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface ApplyingFilterEventHandler extends EventHandler {
    @Subscribe
    void handle(ApplyingFilterEvent e);
}
