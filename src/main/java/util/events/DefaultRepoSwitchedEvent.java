package util.events;

public class DefaultRepoSwitchedEvent extends Event {
    public final String newDefaultRepoId;
    public DefaultRepoSwitchedEvent(String repoId) {
        this.newDefaultRepoId = repoId;
    }
}
