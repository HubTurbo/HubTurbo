package util.events.testevents;

import backend.RepoId;
import util.events.Event;

public class PrimaryRepoChangedEvent extends Event {
    public final RepoId repoId;

    public PrimaryRepoChangedEvent(RepoId repoId) {
        this.repoId = repoId;
    }
}
