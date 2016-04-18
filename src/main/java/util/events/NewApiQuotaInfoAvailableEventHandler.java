package util.events;

import com.google.common.eventbus.Subscribe;

/**
 * Handles the NewApiQuotaInfoAvailableEvent event.
 */
@FunctionalInterface
public interface NewApiQuotaInfoAvailableEventHandler extends EventHandler {
    @Subscribe
    void handle(NewApiQuotaInfoAvailableEvent e);
}
