package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface ShowCommentViewerEventHandler extends EventHandler {
    @Subscribe
    void handle(ShowCommentViewerEvent e);
}
