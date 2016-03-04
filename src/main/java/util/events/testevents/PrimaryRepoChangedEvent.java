package util.events.testevents;

import util.events.UnusedStoredReposChangedEvent;

public class PrimaryRepoChangedEvent extends UnusedStoredReposChangedEvent {
    public final String repoId;

    public PrimaryRepoChangedEvent(String repoId) {
        this.repoId = repoId;
    }
}
