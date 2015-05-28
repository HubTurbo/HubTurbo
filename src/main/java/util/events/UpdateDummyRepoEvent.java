package util.events;

public class UpdateDummyRepoEvent extends Event {

    public final UpdateType updateType;

    public enum UpdateType {NEW_ISSUE}

    public UpdateDummyRepoEvent(UpdateType updateType) {
        this.updateType = updateType;
    }
}
