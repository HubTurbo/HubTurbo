package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface PanelReloadingEventHandler extends EventHandler {
    @Subscribe
    void handle(PanelReloadingEvent e);
}
