package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface IssueSelectedEventHandler extends EventHandler {
    @Subscribe
    void handle(IssueSelectedEvent e);
}
