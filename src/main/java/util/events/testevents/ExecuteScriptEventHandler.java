package util.events.testevents;

import com.google.common.eventbus.Subscribe;
import util.events.EventHandler;

@FunctionalInterface
public interface ExecuteScriptEventHandler extends EventHandler {
    @Subscribe
    void handle(ExecuteScriptEvent e);
}
