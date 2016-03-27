package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface ShowIssuePickerEventHandler extends EventHandler {
    @Subscribe
    void handle(ShowIssuePickerEvent e);
}
