package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface UpdateDummyRepoEventHandler extends EventHandler {
    @Subscribe
    public void handle(UpdateDummyRepoEvent e);
}
