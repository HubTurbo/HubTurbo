package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface ClearLogicModelEventHandler extends EventHandler {
    @Subscribe
    public void handle(ClearLogicModelEvent e);
}
