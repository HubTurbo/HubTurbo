package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface WarnUserEventHandler extends EventHandler {
    @Subscribe
    void handle(WarnUserEvent e);
}
