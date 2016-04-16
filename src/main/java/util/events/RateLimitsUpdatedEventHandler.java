package util.events;

import com.google.common.eventbus.Subscribe;

/**
 * Handles the RateLimitsUpdatedEvent event.
 */
@FunctionalInterface
public interface RateLimitsUpdatedEventHandler extends EventHandler {
    @Subscribe
    void handle(RateLimitsUpdatedEvent e);
}
