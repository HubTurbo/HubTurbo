package util.events.testevents;

import util.events.Event;

public final class UpdateDummyRepoEvent extends Event {

    public final UpdateType updateType;
    public final String repoId;
    public final int itemId; // For issues and milestones
    public final String idString; // For labels and users
    public final String updateText;
    public final String actor; // Only for comments

    public enum UpdateType {
        NEW_ISSUE, UPDATE_ISSUE, DELETE_ISSUE,
        NEW_LABEL, DELETE_LABEL,
        NEW_MILESTONE, UPDATE_MILESTONE, DELETE_MILESTONE,
        NEW_USER, DELETE_USER,
        ADD_COMMENT,
        RESET_REPO
    }

    /**
     * Most generic constructor for UpdateDummyRepoEvent.
     *
     * @param updateType The type of update to be carried out.
     * @param repoId     The full name of the repository.
     * @param itemId     The id of the element to be updated or deleted, if necessary.
     * @param updateText The text to update the element with, if necessary.
     */
    private UpdateDummyRepoEvent(UpdateType updateType, String repoId, int itemId, String idString, String updateText,
                                 String actor) {
        this.updateType = updateType;
        this.repoId = repoId;
        this.itemId = itemId;
        this.idString = idString;
        this.updateText = updateText;
        this.actor = actor;
    }

    public static UpdateDummyRepoEvent newIssue(String repoId) {
        return new UpdateDummyRepoEvent(UpdateType.NEW_ISSUE, repoId, -1, null, null, "");
    }

    public static UpdateDummyRepoEvent updateIssue(String repoId, int itemId, String updateText) {
        return new UpdateDummyRepoEvent(UpdateType.UPDATE_ISSUE, repoId, itemId, null, updateText, "");
    }

    public static UpdateDummyRepoEvent deleteIssue(String repoId, int itemId) {
        return new UpdateDummyRepoEvent(UpdateType.DELETE_ISSUE, repoId, itemId, null, null, "");
    }

    public static UpdateDummyRepoEvent newLabel(String repoId) {
        return new UpdateDummyRepoEvent(UpdateType.NEW_LABEL, repoId, -1, null, null, "");
    }

    public static UpdateDummyRepoEvent deleteLabel(String repoId, String idString) {
        return new UpdateDummyRepoEvent(UpdateType.DELETE_LABEL, repoId, -1, idString, null, "");
    }

    public static UpdateDummyRepoEvent newMilestone(String repoId) {
        return new UpdateDummyRepoEvent(UpdateType.NEW_MILESTONE, repoId, -1, null, null, "");
    }

    public static UpdateDummyRepoEvent updateMilestone(String repoId, int itemId, String updateText) {
        return new UpdateDummyRepoEvent(UpdateType.UPDATE_MILESTONE, repoId, itemId, null, updateText, "");
    }

    public static UpdateDummyRepoEvent deleteMilestone(String repoId, int itemId) {
        return new UpdateDummyRepoEvent(UpdateType.DELETE_MILESTONE, repoId, itemId, null, null, "");
    }

    public static UpdateDummyRepoEvent newUser(String repoId) {
        return new UpdateDummyRepoEvent(UpdateType.NEW_USER, repoId, -1, null, null, "");
    }

    public static UpdateDummyRepoEvent deleteUser(String repoId, String idString) {
        return new UpdateDummyRepoEvent(UpdateType.DELETE_USER, repoId, -1, idString, null, "");
    }

    public static UpdateDummyRepoEvent addComment(String repoId, int itemId, String updateText, String actor) {
        return new UpdateDummyRepoEvent(UpdateType.ADD_COMMENT, repoId, itemId, null, updateText, actor);
    }

    public static UpdateDummyRepoEvent resetRepo(String repoId) {
        return new UpdateDummyRepoEvent(UpdateType.RESET_REPO, repoId, -1, null, null, "");
    }
}
