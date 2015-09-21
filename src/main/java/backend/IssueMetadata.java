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

    // This field expresses whether this metadata is known to be the latest
    // at the time of instantiation. It is like a dirty flag which is
    // invalidated when the issue that this metadata is for is updated.

    private final boolean isLatest;

    // Properties computed from events and comments on instantiation, so we
    // don't have to recompute on querying.

    private final LocalDateTime nonSelfUpdatedAt;
    private final int nonSelfCommentCount;

    private final String eventsETag; // Only modified in the DownloadMetadataTask constructor
    private final String commentsETag;

    // Constructor for default use when initializing TurboIssue
    public IssueMetadata() {
        events = new ArrayList<>();
        comments = new ArrayList<>();

        nonSelfUpdatedAt = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.ofHours(0));
        nonSelfCommentCount = 0;

        eventsETag = "";
        commentsETag = "";
        isLatest = false;
    }

    // Copy constructor used in TurboIssue
    public IssueMetadata(IssueMetadata other) {
        this.events = new ArrayList<>(other.events);
        this.comments = new ArrayList<>(other.comments);

        this.nonSelfUpdatedAt = other.nonSelfUpdatedAt;
        this.nonSelfCommentCount  = other.nonSelfCommentCount;

        this.eventsETag = other.eventsETag;
        this.commentsETag = other.commentsETag;
        this.isLatest = other.isLatest;
    }

    // Copy constructor used in reconciliation
    public IssueMetadata(IssueMetadata other, boolean isLatest) {
        this.events = new ArrayList<>(other.events);
        this.comments = new ArrayList<>(other.comments);

        this.nonSelfUpdatedAt = other.nonSelfUpdatedAt;
        this.nonSelfCommentCount  = other.nonSelfCommentCount;

        this.eventsETag = other.eventsETag;
        this.commentsETag = other.commentsETag;
        this.isLatest = isLatest;
    }

    // Constructor used in DownloadMetadataTask
    public IssueMetadata(List<TurboIssueEvent> events, List<Comment> comments,
                         String eventsETag, String commentsETag) {
        this.events = new ArrayList<>(events);
        this.comments = new ArrayList<>(comments);

        this.nonSelfUpdatedAt = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.ofHours(0)); // Not calculated yet
        this.nonSelfCommentCount = 0; // Not calculated yet

        this.eventsETag = eventsETag;
        this.commentsETag = commentsETag;
        this.isLatest = false;
    }

    // Constructor used in Logic
    public IssueMetadata(IssueMetadata existingMetadata, String currentUser) {
        Date lastNonSelfUpdate = new Date(0);
        Date lastSelfUpdate = new Date(0);
        for (TurboIssueEvent event : existingMetadata.getEvents()) {
            if (isNewEventByOther(currentUser, event, lastNonSelfUpdate)) {
                lastNonSelfUpdate = event.getDate();
            }
            if (isNewEventBySelf(currentUser, event, lastSelfUpdate)){
                lastSelfUpdate = event.getDate();
            }
        }
        for (Comment comment : existingMetadata.getComments()) {
            if (isNewCommentByOther(currentUser, comment, lastNonSelfUpdate)) {
                lastNonSelfUpdate = comment.getCreatedAt();
            }
            if (isNewCommentBySelf(currentUser, comment, lastSelfUpdate)){
                lastSelfUpdate = comment.getCreatedAt();
            }
        }

        this.events = new ArrayList<>(existingMetadata.events);
        this.comments = new ArrayList<>(existingMetadata.comments);
        this.nonSelfUpdatedAt = Utility.dateToLocalDateTime(lastNonSelfUpdate);
        this.nonSelfCommentCount = countCommentsByOthers(existingMetadata.getComments(), currentUser);

        this.eventsETag = existingMetadata.eventsETag;
        this.commentsETag = existingMetadata.commentsETag;
        this.isLatest = true;
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

    private static int countCommentsByOthers(List<Comment> comments, String user) {
        return comments.size() - countCommentsBySelf(comments, user);
    }

    private static int countCommentsBySelf(List<Comment> comments, String user) {
        return Utility.safeLongToInt((comments.stream().filter(c -> isCommentBySelf(user, c)).count()));
    }

    private static boolean isCommentBySelf(String currentUser, Comment comment){
        return comment.getUser().getLogin().equalsIgnoreCase(currentUser);
    }

    // Constructor used in MultiModel
    public IssueMetadata(IssueMetadata other, LocalDateTime nonSelfUpdatedAt,
                         List<TurboIssueEvent> currEvents, String currEventsETag) {
        if (currEventsETag.equals(other.eventsETag)) {
            events = new ArrayList<>(currEvents);
        } else {
            events = new ArrayList<>(other.events);
        }
        this.comments = new ArrayList<>(other.comments);

        this.nonSelfUpdatedAt = nonSelfUpdatedAt; // After creation date reconciliation
        this.nonSelfCommentCount  = other.nonSelfCommentCount;

        this.eventsETag = currEventsETag;
        this.commentsETag = other.commentsETag;
        this.isLatest = other.isLatest;
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

    public boolean isLatest() {
        return isLatest;
    }

    public LocalDateTime getNonSelfUpdatedAt() {
        return nonSelfUpdatedAt;
    }

    public int getNonSelfCommentCount() {
        return nonSelfCommentCount;
    }

    public String getEventsETag() {
        return eventsETag;
    }

    public String getCommentsETag() {
        return commentsETag;
    }

    @Override
    public String toString() {
        return "Events: " + events.toString() + ", " + "comments: " + comments.toString();
    }
}
