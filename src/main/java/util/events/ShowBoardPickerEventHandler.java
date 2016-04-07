package util.events;

import com.google.common.eventbus.Subscribe;

/**
 * Represents the event handler of {@link ShowBoardPickerEvent}.
 */
@FunctionalInterface
public interface ShowBoardPickerEventHandler extends EventHandler {
    @Subscribe
    void handle(ShowBoardPickerEvent e);
}
