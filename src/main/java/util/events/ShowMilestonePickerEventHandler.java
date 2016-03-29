package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface ShowMilestonePickerEventHandler extends EventHandler {
    @Subscribe
    void handle(ShowMilestonePickerEvent e);
}
