package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface ShowRenamePanelEventHandler extends EventHandler {
    @Subscribe
    void handle(ShowRenamePanelEvent e);
}
