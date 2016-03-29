package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface FilterExceptionEventHandler extends EventHandler {
    @Subscribe
    void handle(FilterExceptionEvent e);
}
