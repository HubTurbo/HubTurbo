package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface ShowRenameTextFieldEventHandler extends EventHandler {
    @Subscribe
    void handle(ShowRenameTextFieldEvent e);
}
