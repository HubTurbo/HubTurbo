package util.events;

public class UpdateDummyRepoEvent extends Event {

    public final UpdateType updateType;
    public final String repoId;
    public final int itemId;
    public final String updateText;

    public enum UpdateType {NEW_ISSUE, UPDATE_ISSUE, DELETE_ISSUE,
        NEW_LABEL, UPDATE_LABEL, DELETE_LABEL,
        NEW_MILESTONE, UPDATE_MILESTONE, DELETE_MILESTONE,
        NEW_USER, UPDATE_USER, DELETE_USER
    }

    /**
     * Most generic constructor for UpdateDummyRepoEvent.
     *
     * @param updateType The type of update to be carried out.
     * @param repoId The full name of the repository.
     * @param itemId The id of the element to be updated or deleted, if necessary.
     * @param updateText The text to update the element with, if necessary.
     */
    public UpdateDummyRepoEvent(UpdateType updateType, String repoId, int itemId, String updateText) {
        this.updateType = updateType;
        this.repoId = repoId;
        this.itemId = itemId;
        this.updateText = updateText;
    }

    // Overloaded constructors

    public UpdateDummyRepoEvent(UpdateType updateType) {
        this(updateType, null, -1, null);
    }

    public UpdateDummyRepoEvent(UpdateType updateType, String repoId) {
        this(updateType, repoId, -1, null);
    }

    public UpdateDummyRepoEvent(UpdateType updateType, String repoId, int itemId) {
        this(updateType, repoId, itemId, null);
    }
}
