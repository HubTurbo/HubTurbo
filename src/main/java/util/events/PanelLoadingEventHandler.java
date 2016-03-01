package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface PanelLoadingEventHandler extends EventHandler {
    @Subscribe
    void handle(PanelLoadingEvent e);
}
