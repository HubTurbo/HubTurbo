package util.events.testevents;

import util.events.Event;

public class NavigateToPageEvent extends Event {
    public final String url;

    public NavigateToPageEvent(String url) {
        this.url = url;
    }
}
