package util.events;

import com.google.common.eventbus.Subscribe;

/**
 * An EventHandler to handle the UpdateSyncRefreshRateEvent event.
 */
@FunctionalInterface
public interface UpdateSyncRefreshRateEventHandler extends EventHandler {
    @Subscribe
    void handle(UpdateSyncRefreshRateEvent e);
}
