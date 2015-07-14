package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface UpdateRateLimitsEventHandler extends EventHandler {
    @Subscribe
    void handle(UpdateRateLimitsEvent e);
}
