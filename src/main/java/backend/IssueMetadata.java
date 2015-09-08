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
    private final int nonSelfCommentCount;

    // If isUpdated is true, nonSelfUpdatedAt will be used to sort/filter instead of updatedAt in TurboIssue
    private final boolean isUpdated;
    private final String ETag;

    // Constructor for default use when initializing TurboIssue
    public IssueMetadata() {
        events = new ArrayList<>();
        comments = new ArrayList<>();
        nonSelfUpdatedAt = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.ofHours(0));
        nonSelfCommentCount = 0;
        isUpdated = false;
        ETag = "";
    }

    // Copy constructor used in TurboIssue
    public IssueMetadata(IssueMetadata other) {
        this.events = new ArrayList<>(other.events);
        this.comments = new ArrayList<>(other.comments);
        this.nonSelfUpdatedAt = other.nonSelfUpdatedAt;
        this.nonSelfCommentCount  = other.nonSelfCommentCount;
        this.isUpdated = other.isUpdated;
        this.ETag = other.ETag;
    }

    // Copy constructor used in reconciliation
    public IssueMetadata(IssueMetadata other, boolean isUpdated) {
        this.events = new ArrayList<>(other.events);
        this.comments = new ArrayList<>(other.comments);
        this.nonSelfUpdatedAt = other.nonSelfUpdatedAt;
        this.nonSelfCommentCount  = other.nonSelfCommentCount;
        this.isUpdated = isUpdated;
        this.ETag = other.ETag;
    }

    // Constructor used in DownloadMetadataTask
    public IssueMetadata(List<TurboIssueEvent> events, List<Comment> comments, String ETag) {
        this.events = events;
        this.comments = comments;
        this.nonSelfUpdatedAt = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.ofHours(0)); // Not calculated yet
        this.nonSelfCommentCount = 0; // Not calculated yet
        this.isUpdated = false;
        this.ETag = ETag;
    }

    // Constructor used in Logic
    public IssueMetadata(IssueMetadata other, LocalDateTime nonSelfUpdatedAt, int nonSelfCommentCount) {
        this.events = new ArrayList<>(other.events);
        this.comments = new ArrayList<>(other.comments);
        this.nonSelfUpdatedAt = nonSelfUpdatedAt; // Calculated just prior to calling this constructor
        this.nonSelfCommentCount = nonSelfCommentCount; // Calculated just prior to calling
        this.isUpdated = true;
        this.ETag = other.ETag;
    }

    // Constructor used in MultiModel
    public IssueMetadata(IssueMetadata other, LocalDateTime nonSelfUpdatedAt) {
        this.events = new ArrayList<>(other.events);
        this.comments = new ArrayList<>(other.comments);
        this.nonSelfUpdatedAt = nonSelfUpdatedAt; // After creation date reconciliation
        this.nonSelfCommentCount  = other.nonSelfCommentCount;
        this.isUpdated = other.isUpdated;
        this.ETag = other.ETag;
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

    public int getNonSelfCommentCount() {
        return nonSelfCommentCount;
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    public String getETag() {
        return ETag;
    }

    @Override
    public String toString() {
        return "Events: " + events.toString() + ", " + "comments: " + comments.toString();
    }
}
