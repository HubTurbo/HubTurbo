package util.events.testevents;

import util.events.Event;

public class WindowResizeEvent extends Event {

    public enum EventType {NONE, MAXIMIZE_WINDOW, MINIMIZE_WINDOW, DEFAULT_SIZE_WINDOW}

    public final EventType eventType;

    public WindowResizeEvent(EventType eventType) {
        this.eventType = eventType;
    }
}
