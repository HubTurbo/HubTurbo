package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface ShowBoardPickerEventHandler extends EventHandler {
    @Subscribe
    void handle(ShowBoardPickerEvent e);
}
