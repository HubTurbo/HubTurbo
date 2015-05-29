package util.events;

public class ClearLogicModelEvent extends Event {

    public final String repoId;

    /**
     * Should only be used from DummyRepo to tell Logic that repo is ready to be
     * re-"downloaded".
     *
     * @param repoId The repo to be cleared (and later downloaded again)
     */
    public ClearLogicModelEvent(String repoId) {
        this.repoId = repoId;
    }
}
