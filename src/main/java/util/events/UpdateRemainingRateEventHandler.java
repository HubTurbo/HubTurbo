package util.events;

import com.google.common.eventbus.Subscribe;

/**
 * This is an EventHandler to handle the UpdateRemainingRateEvent event.
 */
@FunctionalInterface
public interface UpdateRemainingRateEventHandler extends EventHandler {
    @Subscribe
    void handle(UpdateRemainingRateEvent e);
}
