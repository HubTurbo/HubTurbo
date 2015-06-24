package util.events.testevents;

import com.google.common.eventbus.Subscribe;
import util.events.EventHandler;

@FunctionalInterface
public interface JumpToCommentEventHandler extends EventHandler {
    @Subscribe
    void handle(JumpToCommentEvent e);
}
