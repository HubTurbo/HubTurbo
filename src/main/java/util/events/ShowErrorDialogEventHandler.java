package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface ShowErrorDialogEventHandler extends EventHandler {
    @Subscribe
    void handle(ShowErrorDialogEvent e);
}
