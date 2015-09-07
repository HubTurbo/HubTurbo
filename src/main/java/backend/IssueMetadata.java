package backend;

import github.TurboIssueEvent;
import org.eclipse.egit.github.core.Comment;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class IssueMetadata {
    private final List<TurboIssueEvent> events;
    private final List<Comment> comments;
    private final LocalDateTime nonSelfUpdatedAt;
    private final LocalDateTime selfUpdatedAt;
    private final int nonSelfCommentCount;
    private final int selfCommentCount;

    // If isUpdated is true, nonSelfUpdatedAt will be used to sort/filter instead of updatedAt in TurboIssue
    private final boolean isUpdated;
    private final boolean isUpdatedBySelf;
    private final boolean isUpdatedByOthers;

    // Constructor for default use when initializing TurboIssue
    public IssueMetadata() {
        events = new ArrayList<>();
        comments = new ArrayList<>();
        nonSelfUpdatedAt = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.ofHours(0));
        selfUpdatedAt = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.ofHours(0));
        nonSelfCommentCount = 0;
        selfCommentCount = 0;
        isUpdated = false;
        isUpdatedByOthers = false;
        isUpdatedBySelf = false;
    }

    // Copy constructor used in TurboIssue
    public IssueMetadata(IssueMetadata other) {
        this.events = new ArrayList<>(other.events);
        this.comments = new ArrayList<>(other.comments);
        this.nonSelfUpdatedAt = other.nonSelfUpdatedAt;
        this.selfUpdatedAt = other.selfUpdatedAt;
        this.nonSelfCommentCount  = other.nonSelfCommentCount;
        this.selfCommentCount = other.selfCommentCount;
        this.isUpdated = other.isUpdated;
        this.isUpdatedByOthers = other.isUpdatedByOthers;
        this.isUpdatedBySelf = other.isUpdatedBySelf;
    }

    // Copy constructor used in reconciliation
    public IssueMetadata(IssueMetadata other, boolean isUpdated) {
        this.events = new ArrayList<>(other.events);
        this.comments = new ArrayList<>(other.comments);
        this.nonSelfUpdatedAt = other.nonSelfUpdatedAt;
        this.selfUpdatedAt = other.selfUpdatedAt;
        this.nonSelfCommentCount  = other.nonSelfCommentCount;
        this.selfCommentCount  = other.selfCommentCount;
        this.isUpdated = isUpdated;
        this.isUpdatedByOthers = other.isUpdatedByOthers;
        this.isUpdatedBySelf = other.isUpdatedBySelf;
    }

    // Constructor used in DownloadMetadataTask
    public IssueMetadata(List<TurboIssueEvent> events, List<Comment> comments) {
        this.events = events;
        this.comments = comments;
        this.nonSelfUpdatedAt = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.ofHours(0)); // Not calculated yet
        this.selfUpdatedAt = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.ofHours(0)); // Not calculated yet
        this.nonSelfCommentCount = 0; // Not calculated yet
        this.selfCommentCount = 0; // Not calculated yet
        this.isUpdated = false;
        this.isUpdatedByOthers = false;
        this.isUpdatedBySelf = false;
    }

    // Constructor used in Logic
    public IssueMetadata(IssueMetadata other, LocalDateTime nonSelfUpdatedAt, LocalDateTime selfUpdatedAt,
                         int nonSelfCommentCount, int selfCommentCount,
                         boolean isUpdatedByOthers, boolean isUpdatedBySelf) {
        this.events = new ArrayList<>(other.events);
        this.comments = new ArrayList<>(other.comments);
        this.nonSelfUpdatedAt = nonSelfUpdatedAt; // Calculated just prior to calling this constructor
        this.selfUpdatedAt = selfUpdatedAt;
        this.nonSelfCommentCount = nonSelfCommentCount;
        this.selfCommentCount = selfCommentCount;
        this.isUpdated = true;
        this.isUpdatedByOthers = isUpdatedByOthers;
        this.isUpdatedBySelf = isUpdatedBySelf;
    }

    // Constructor used in MultiModel
    public IssueMetadata(IssueMetadata other, LocalDateTime nonSelfUpdatedAt) {
        this.events = new ArrayList<>(other.events);
        this.comments = new ArrayList<>(other.comments);
        this.nonSelfUpdatedAt = nonSelfUpdatedAt; // After creation date reconciliation
        this.selfUpdatedAt = other.selfUpdatedAt;
        this.nonSelfCommentCount  = other.nonSelfCommentCount;
        this.selfCommentCount = other.selfCommentCount;
        this.isUpdated = other.isUpdated;
        this.isUpdatedBySelf = other.isUpdatedBySelf;
        this.isUpdatedByOthers = other.isUpdatedByOthers;
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

    public boolean isUpdated() {
        return isUpdated;
    }

    public boolean isUpdatedByOthers() {
        return isUpdatedByOthers;
    }

    public boolean isUpdatedBySelf() {
        return isUpdatedBySelf;
    }

    @Override
    public String toString() {
        return "Events: " + events.toString() + ", " + "comments: " + comments.toString();
    }
}
