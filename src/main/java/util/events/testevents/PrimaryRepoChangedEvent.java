package util.events.testevents;

import util.events.Event;

public class PrimaryRepoChangedEvent extends Event {
    public final String repoId;

    public PrimaryRepoChangedEvent(String repoId) {
        this.repoId = repoId;
    }
}
