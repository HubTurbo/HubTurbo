package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface ShowIssueCreatorEventHandler extends EventHandler {
    @Subscribe
    void handle(ShowIssueCreatorEvent e);
}
