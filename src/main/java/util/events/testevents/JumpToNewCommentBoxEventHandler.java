package util.events.testevents;

import com.google.common.eventbus.Subscribe;
import util.events.EventHandler;

@FunctionalInterface
public interface JumpToNewCommentBoxEventHandler extends EventHandler {
    @Subscribe
    void handle(JumpToNewCommentBoxEvent e);
}
