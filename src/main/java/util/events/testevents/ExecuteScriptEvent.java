package util.events.testevents;

import util.events.Event;

public class ExecuteScriptEvent extends Event {
    public final String script;

    public ExecuteScriptEvent(String script) {
        this.script = script;
    }
}
