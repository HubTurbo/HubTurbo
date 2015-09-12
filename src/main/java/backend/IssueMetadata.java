package backend;

import github.TurboIssueEvent;
import org.eclipse.egit.github.core.Comment;

import util.Utility;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class IssueMetadata {
    private final List<TurboIssueEvent> events;
    private final List<Comment> comments;
    private final LocalDateTime nonSelfUpdatedAt;
    private final LocalDateTime selfUpdatedAt;
    private final int nonSelfCommentCount;
    private final int selfCommentCount;

    private final boolean isUpdatedBySelf; // for update by self
    private final boolean isUpdatedByOthers; // for update by others

    private enum UpdatedKind {
        UPDATED_BY_SELF, UPDATED_BY_OTHER
    }

    private final String ETag; // Only modified in the DownloadMetadataTask constructor

    // Constructor for default use when initializing TurboIssue
    public IssueMetadata() {
        events = new ArrayList<>();
        comments = new ArrayList<>();

        nonSelfUpdatedAt = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.ofHours(0));
        selfUpdatedAt = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.ofHours(0));
        nonSelfCommentCount = 0;
        selfCommentCount = 0;
        isUpdatedByOthers = false;
        isUpdatedBySelf = false;

        ETag = "";
    }

    // Copy constructor used in TurboIssue
    public IssueMetadata(IssueMetadata other) {
        this.events = new ArrayList<>(other.events);
        this.comments = new ArrayList<>(other.comments);

        this.nonSelfUpdatedAt = other.nonSelfUpdatedAt;
        this.selfUpdatedAt = other.selfUpdatedAt;
        this.nonSelfCommentCount  = other.nonSelfCommentCount;
        this.selfCommentCount = other.selfCommentCount;
        this.isUpdatedByOthers = other.isUpdatedByOthers;
        this.isUpdatedBySelf = other.isUpdatedBySelf;

        this.ETag = other.ETag;
    }

    // Copy constructor used in reconciliation
    public IssueMetadata(IssueMetadata other, boolean isUpdated) {
        this.events = new ArrayList<>(other.events);
        this.comments = new ArrayList<>(other.comments);

        this.nonSelfUpdatedAt = other.nonSelfUpdatedAt;
        this.selfUpdatedAt = other.selfUpdatedAt;
        this.nonSelfCommentCount  = other.nonSelfCommentCount;
        this.selfCommentCount  = other.selfCommentCount;
        this.isUpdatedByOthers = other.isUpdatedByOthers;
        this.isUpdatedBySelf = other.isUpdatedBySelf;

        this.ETag = other.ETag;
    }

    // Constructor used in DownloadMetadataTask
    public IssueMetadata(List<TurboIssueEvent> events, List<Comment> comments, String ETag) {
        this.events = events;
        this.comments = comments;

        this.nonSelfUpdatedAt = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.ofHours(0)); // Not calculated yet
        this.selfUpdatedAt = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.ofHours(0)); // Not calculated yet
        this.nonSelfCommentCount = 0; // Not calculated yet
        this.selfCommentCount = 0; // Not calculated yet
        this.isUpdatedByOthers = false;
        this.isUpdatedBySelf = false;

        this.ETag = ETag;
    }

    // Constructor used in Logic
    public IssueMetadata(IssueMetadata existingMetadata, String currentUser) {
        Date lastNonSelfUpdate = new Date(0);
        Date lastSelfUpdate = new Date(0);
        boolean isUpdatedByOthers = false;
        boolean isUpdatedBySelf = false;
        for (TurboIssueEvent event : existingMetadata.getEvents()) {
            if (isNewEventByOther(currentUser, event, lastNonSelfUpdate)) {
                lastNonSelfUpdate = event.getDate();
                isUpdatedByOthers = true;
            }
            if (isNewEventBySelf(currentUser, event, lastSelfUpdate)){
                lastSelfUpdate = event.getDate();
                isUpdatedBySelf = true;
            }
        }
        for (Comment comment : existingMetadata.getComments()) {
            if (isNewCommentByOther(currentUser, comment, lastNonSelfUpdate)) {
                lastNonSelfUpdate = comment.getCreatedAt();
                isUpdatedByOthers = true;
            }
            if (isNewCommentBySelf(currentUser, comment, lastSelfUpdate)){
                lastSelfUpdate = comment.getCreatedAt();
                isUpdatedBySelf = true;
            }
        }

        this.events = new ArrayList<>(existingMetadata.events);
        this.comments = new ArrayList<>(existingMetadata.comments);
        this.nonSelfUpdatedAt = Utility.dateToLocalDateTime(lastNonSelfUpdate);
        this.selfUpdatedAt = Utility.dateToLocalDateTime(lastSelfUpdate);
        this.nonSelfCommentCount = calculateCommentCount(existingMetadata.getComments(), currentUser,
                UpdatedKind.UPDATED_BY_OTHER);
        this.selfCommentCount = calculateCommentCount(existingMetadata.getComments(), currentUser,
                UpdatedKind.UPDATED_BY_SELF);
        this.isUpdatedByOthers = isUpdatedByOthers;
        this.isUpdatedBySelf = isUpdatedBySelf;

        this.ETag = existingMetadata.ETag;
    }

    private static boolean isNewCommentByOther(String currentUser, Comment comment, Date lastNonSelfUpdate){
        return !comment.getUser().getLogin().equalsIgnoreCase(currentUser)
                && comment.getCreatedAt().after(lastNonSelfUpdate);
    }

    private static boolean isNewCommentBySelf(String currentUser, Comment comment, Date lastSelfUpdate){
        return comment.getUser().getLogin().equalsIgnoreCase(currentUser)
                && comment.getCreatedAt().after(lastSelfUpdate);
    }

    private static boolean isNewEventBySelf(String currentUser, TurboIssueEvent event, Date lastSelfUpdate){
        return event.getActor().getLogin().equalsIgnoreCase(currentUser)
                && event.getDate().after(lastSelfUpdate);
    }

    private static boolean isNewEventByOther(String currentUser, TurboIssueEvent event, Date lastNonSelfUpdate){
        return !event.getActor().getLogin().equalsIgnoreCase(currentUser)
                && event.getDate().after(lastNonSelfUpdate);
    }

    private static int calculateCommentCount(List<Comment> comments, String currentUser, UpdatedKind updatedKind) {
        int result = 0;
        if (updatedKind == UpdatedKind.UPDATED_BY_OTHER) {
            return Utility.safeLongToInt((comments.stream().filter(c -> !isCommentBySelf(currentUser, c)).count()));
        } else if (updatedKind == UpdatedKind.UPDATED_BY_SELF){
            return Utility.safeLongToInt((comments.stream().filter(c -> isCommentBySelf(currentUser, c)).count()));
        }
        return result;
    }

    private static boolean isCommentBySelf(String currentUser, Comment comment){
        return comment.getUser().getLogin().equalsIgnoreCase(currentUser);
    }

    // Constructor used in MultiModel
    public IssueMetadata(IssueMetadata other, LocalDateTime nonSelfUpdatedAt) {
        this.events = new ArrayList<>(other.events);
        this.comments = new ArrayList<>(other.comments);

        this.nonSelfUpdatedAt = nonSelfUpdatedAt; // After creation date reconciliation
        this.selfUpdatedAt = other.selfUpdatedAt;
        this.nonSelfCommentCount  = other.nonSelfCommentCount;
        this.selfCommentCount = other.selfCommentCount;
        this.isUpdatedBySelf = other.isUpdatedBySelf;
        this.isUpdatedByOthers = other.isUpdatedByOthers;

        this.ETag = other.ETag;
    }

    //Constructor used in FilterEvalTests
    public IssueMetadata(IssueMetadata other, LocalDateTime nonSelfUpdatedAt, LocalDateTime selfUpdatedAt,
                         int nonSelfCommentCount, int selfCommentCount,
                         boolean isUpdatedByOthers, boolean isUpdatedBySelf) {
        this.events = new ArrayList<>(other.events);
        this.comments = new ArrayList<>(other.comments);

        this.nonSelfUpdatedAt = nonSelfUpdatedAt; // Calculated just prior to calling this constructor
        this.selfUpdatedAt = selfUpdatedAt;
        this.nonSelfCommentCount = nonSelfCommentCount;
        this.selfCommentCount = selfCommentCount;
        this.isUpdatedBySelf = isUpdatedBySelf;
        this.isUpdatedByOthers = isUpdatedByOthers;

        this.ETag= "";
    }

    public String summarise() {
        return String.format("%d events, %d comments", events.size(), comments.size());
    }

    public List<TurboIssueEvent> getEvents() {
        return new ArrayList<>(events);
    }

    public List<Comment> getComments() {
        return new ArrayList<>(comments);
    }

    public LocalDateTime getNonSelfUpdatedAt() {
        return nonSelfUpdatedAt;
    }

    public LocalDateTime getSelfUpdatedAt() {
        return selfUpdatedAt;
    }

    public int getNonSelfCommentCount() {
        return nonSelfCommentCount;
    }

    public int getSelfCommentCount() {
        return selfCommentCount;
    }

    public boolean isUpdatedByOthers() {
        return isUpdatedByOthers;
    }

    public boolean isUpdatedBySelf() {
        return isUpdatedBySelf;
    }

    public String getETag() {
        return ETag;
    }

    @Override
    public String toString() {
        return "Events: " + events.toString() + ", " + "comments: " + comments.toString();
    }
}
