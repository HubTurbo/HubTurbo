package util.events.testevents;

import util.events.Event;

public class SendKeysToBrowserEvent extends Event {
    public final String keyCode;

    public SendKeysToBrowserEvent(String keyCode) {
        this.keyCode = keyCode;
    }
}
