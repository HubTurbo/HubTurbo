package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface BoardSavedEventHandler extends EventHandler {
    @Subscribe
    void handle(BoardSavedEvent e);
}
