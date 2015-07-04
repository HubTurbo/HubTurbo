package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface PanelClickedEventHandler extends EventHandler {
    @Subscribe
    void handle(PanelClickedEvent e);
}
