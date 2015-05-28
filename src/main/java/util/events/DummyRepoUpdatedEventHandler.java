package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface DummyRepoUpdatedEventHandler extends EventHandler {
    @Subscribe
    public void handle(DummyRepoUpdatedEvent e);
}