package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface PanelReloadedEventHandler extends EventHandler {
    @Subscribe
    void handle(PanelReloadedEvent e);
}
