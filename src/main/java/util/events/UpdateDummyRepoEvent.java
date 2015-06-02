package util.events;

public class UpdateDummyRepoEvent extends Event {

    public final UpdateType updateType;
    public final String repoId;
    public final int itemId; // For issues and milestones
    public final String idString; // For labels and users
    public final String updateText;

    public enum UpdateType {NEW_ISSUE, UPDATE_ISSUE, DELETE_ISSUE,
        NEW_LABEL, DELETE_LABEL,
        NEW_MILESTONE, UPDATE_MILESTONE, DELETE_MILESTONE,
        NEW_USER, DELETE_USER,
        RESET_REPO
    }

    /**
     * Most generic constructor for UpdateDummyRepoEvent.
     *
     * @param updateType The type of update to be carried out.
     * @param repoId The full name of the repository.
     * @param itemId The id of the element to be updated or deleted, if necessary.
     * @param updateText The text to update the element with, if necessary.
     */
    public UpdateDummyRepoEvent(UpdateType updateType, String repoId, int itemId, String idString, String updateText) {
        this.updateType = updateType;
        this.repoId = repoId;
        this.itemId = itemId;
        this.idString = idString;
        this.updateText = updateText;
    }

    // Overloaded constructors

    public UpdateDummyRepoEvent(UpdateType updateType) {
        this(updateType, null, -1, null, null);
    }

    public UpdateDummyRepoEvent(UpdateType updateType, String repoId) {
        this(updateType, repoId, -1, null, null);
    }

    public UpdateDummyRepoEvent(UpdateType updateType, String repoId, int itemId) {
        this(updateType, repoId, itemId, null, null);
    }

    public UpdateDummyRepoEvent(UpdateType updateType, String repoId, String idString) {
        this(updateType, repoId, -1, idString, null);
    }
}
