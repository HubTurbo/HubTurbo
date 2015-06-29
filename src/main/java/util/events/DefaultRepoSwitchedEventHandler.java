package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface DefaultRepoSwitchedEventHandler extends EventHandler {
    @Subscribe
    void handle(DefaultRepoSwitchedEvent e);
}
