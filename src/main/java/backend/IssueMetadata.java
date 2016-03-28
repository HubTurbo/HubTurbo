package backend;

import github.TurboIssueEvent;
import org.eclipse.egit.github.core.Comment;

import util.Utility;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class IssueMetadata {

    private final List<TurboIssueEvent> events;
    private final List<Comment> comments;

    // This field expresses whether this metadata is known to be the latest
    // at the time of instantiation. It is like a dirty flag which is
    // invalidated when the issue that this metadata is for is updated.

    private final boolean isLatest;

    // Properties computed from events and comments on instantiation, so we
    // don't have to recompute on querying.

    private final String user;
    private final LocalDateTime nonSelfUpdatedAt;
    private final int nonSelfCommentCount;

    private final String eventsETag; // Only modified in the DownloadMetadataTask constructor
    private final String commentsETag;

    /**
     * Factory method for the empty metadata instance. This is used as the default for
     * new issues.
     */
    public static IssueMetadata empty() {
        return new IssueMetadata(new ArrayList<>(), new ArrayList<>(), false, "", "");
    }

    /**
     * Invalidates a metadata instance. This occurs after issue updates are reconciled; in
     * that case we assume the metadata is no longer the latest.
     */
    public IssueMetadata invalidate() {
        return new IssueMetadata(events, comments, false, eventsETag, commentsETag, user);
    }

    /**
     * Constructs an intermediate metadata instance. Intermediate metadata does not have
     * computed properties filled in; the name of the current user is required for that.
     * Intermediate instances are constructed immediately upon download. The current user
     * is filled in later.
     */
    public static IssueMetadata intermediate(List<TurboIssueEvent> events, List<Comment> comments,
                                             String eventsETag, String commentsETag) {
        return new IssueMetadata(events, comments, false, eventsETag, commentsETag);
    }

    /**
     * Fills in the current user for an intermediate metadata instance, computing properties
     * and turning it into a full metadata instance.
     * May also be used to change the perspective of a full metadata instance, but that's
     * not very interesting.
     */
    public IssueMetadata full(String currentUser) {
        return new IssueMetadata(events, comments, true, eventsETag, commentsETag, currentUser);
    }

    /**
     * Reconciles a newly-updated metadata instance against older data.
     */
    public IssueMetadata reconcile(LocalDateTime nonSelfUpdatedAt,
                                   List<TurboIssueEvent> existingEvents, String existingETag) {
        List<TurboIssueEvent> newEvents;
        if (existingETag.equals(eventsETag)) {
            newEvents = new ArrayList<>(existingEvents);
        } else {
            newEvents = new ArrayList<>(events);
        }
        return new IssueMetadata(newEvents, comments, isLatest, eventsETag, commentsETag, nonSelfUpdatedAt, user);
    }

    /**
     * Intermediate metadata constructor (no user provided, empty computed properties)
     */
    private IssueMetadata(List<TurboIssueEvent> events, List<Comment> comments,
                          boolean isLatest, String eventsETag, String commentsETag) {
        this.events = new ArrayList<>(events);
        this.comments = new ArrayList<>(comments);
        this.isLatest = isLatest;
        this.eventsETag = eventsETag;
        this.commentsETag = commentsETag;

        this.user = "";
        this.nonSelfUpdatedAt = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.ofHours(0));
        this.nonSelfCommentCount = 0;
    }

    /**
     * Full metadata constructor (user provided, computed properties present)
     */
    private IssueMetadata(List<TurboIssueEvent> events, List<Comment> comments,
                          boolean isLatest, String eventsETag, String commentsETag,
                          String user) {
        this(events, comments, isLatest, eventsETag, commentsETag,
             computeNonSelfUpdatedAt(events, comments, user), user);
    }

    /**
     * Full metadata constructor with nonSelfUpdateTime left out
     */
    private IssueMetadata(List<TurboIssueEvent> events, List<Comment> comments,
                          boolean isLatest, String eventsETag, String commentsETag,
                          LocalDateTime nonSelfUpdatedAt, String user) {
        this.events = new ArrayList<>(events);
        this.comments = new ArrayList<>(comments);
        this.isLatest = isLatest;
        this.eventsETag = eventsETag;
        this.commentsETag = commentsETag;

        this.user = user;
        this.nonSelfUpdatedAt = nonSelfUpdatedAt;
        this.nonSelfCommentCount = countCommentsByOthers(comments, user);
    }

    private static LocalDateTime computeNonSelfUpdatedAt(List<TurboIssueEvent> events, List<Comment> comments,
                                                         String user) {
        Date result = new Date(0);
        for (TurboIssueEvent event : events) {
            if (isEventByOthers(event, user) && event.getDate().after(result)) {
                result = event.getDate();
            }
        }
        for (Comment comment : comments) {
            if (isCommentByOthers(comment, user) && comment.getCreatedAt().after(result)) {
                result = comment.getCreatedAt();
            }
        }
        return Utility.dateToLocalDateTime(result);
    }

    private static boolean isCommentBySelf(Comment comment, String user) {
        return comment.getUser().getLogin().equalsIgnoreCase(user);
    }

    private static boolean isCommentByOthers(Comment comment, String user) {
        return !isCommentBySelf(comment, user);
    }

    private static int countCommentsBySelf(List<Comment> comments, String user) {
        return Utility.safeLongToInt(comments.stream()
                                             .filter(c -> isCommentBySelf(c, user))
                                             .count());
    }

    private static boolean isEventBySelf(TurboIssueEvent event, String user) {
        return event.getActor().getLogin().equalsIgnoreCase(user);
    }

    private static boolean isEventByOthers(TurboIssueEvent event, String user) {
        return !isEventBySelf(event, user);
    }

    private static int countCommentsByOthers(List<Comment> comments, String user) {
        return comments.size() - countCommentsBySelf(comments, user);
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
