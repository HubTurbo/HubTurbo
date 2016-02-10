package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface ShowAssigneePickerEventHandler extends EventHandler {
    @Subscribe
    void handle(ShowAssigneePickerEvent e);
}
