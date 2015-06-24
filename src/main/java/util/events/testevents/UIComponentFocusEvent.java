package util.events.testevents;

import util.events.Event;

public class UIComponentFocusEvent extends Event {

    public enum EventType {NONE, FILTER_BOX}

    public final EventType eventType;

    public UIComponentFocusEvent(EventType eventType) {
        this.eventType = eventType;
    }
}
