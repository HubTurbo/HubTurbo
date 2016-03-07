package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface ShowRepositoryPickerEventHandler extends EventHandler {
    @Subscribe
    void handle(ShowRepositoryPickerEvent e);
}
