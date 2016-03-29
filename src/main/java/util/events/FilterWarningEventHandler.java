package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface FilterWarningEventHandler extends EventHandler {
    @Subscribe
    void handle(FilterWarningEvent e);
}
