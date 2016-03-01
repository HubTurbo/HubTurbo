package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface PanelLoadedEventHandler extends EventHandler {
    @Subscribe
    void handle(PanelLoadedEvent e);
}
