package util.events.testevents;

import backend.RepoId;
import util.events.Event;

public class ClearLogicModelEvent extends Event {

    public final RepoId repoId;

    /**
     * Should only be used from DummyRepo to tell Logic that repo is ready to be
     * re-"downloaded".
     *
     * @param repoId The repo to be cleared (and later downloaded again)
     */
    public ClearLogicModelEvent(RepoId repoId) {
        this.repoId = repoId;
    }
}
