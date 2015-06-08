package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface IssueCreatedEventHandler extends EventHandler {
    @Subscribe
    void handle(IssueCreatedEvent e);
}
