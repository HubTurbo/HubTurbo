package backend;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.egit.github.core.Comment;

import github.TurboIssueEvent;

public class IssueMetadata {
    private final List<TurboIssueEvent> events;
    private final List<Comment> comments;
    private final LocalDateTime nonSelfUpdatedAt;
    private final int nonSelfCommentCount;
    private final boolean isUpdated;

    // Constructor for default use when initializing TurboIssue
    public IssueMetadata() {
        events = new ArrayList<>();
        comments = new ArrayList<>();
        nonSelfUpdatedAt = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.ofHours(0));
        nonSelfCommentCount = 0;
        isUpdated = false;
    }

    // Copy constructor used in TurboIssue
    public IssueMetadata(IssueMetadata other) {
        this.events = new ArrayList<>(other.events);
        this.comments = new ArrayList<>(other.comments);
        this.nonSelfUpdatedAt = other.nonSelfUpdatedAt;
        this.nonSelfCommentCount  = other.nonSelfCommentCount;
        this.isUpdated = other.isUpdated;
    }

    // Constructor used in DownloadMetadataTask
    public IssueMetadata(List<TurboIssueEvent> events, List<Comment> comments) {
        this.events = events;
        this.comments = comments;
        this.nonSelfUpdatedAt = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.ofHours(0)); // Not calculated yet
        this.nonSelfCommentCount = 0; // Not calculated yet
        this.isUpdated = false;
    }

    // Constructor used in Logic
    public IssueMetadata(IssueMetadata other, LocalDateTime nonSelfUpdatedAt, int nonSelfCommentCount) {
        this.events = new ArrayList<>(other.events);
        this.comments = new ArrayList<>(other.comments);
        this.nonSelfUpdatedAt = nonSelfUpdatedAt; // Calculated just prior to calling this constructor
        this.nonSelfCommentCount = nonSelfCommentCount; // Calculated just prior to calling
        this.isUpdated = true;
    }

    // Constructor used in MultiModel
    public IssueMetadata(IssueMetadata other, LocalDateTime nonSelfUpdatedAt) {
        this.events = new ArrayList<>(other.events);
        this.comments = new ArrayList<>(other.comments);
        this.nonSelfUpdatedAt = nonSelfUpdatedAt; // After creation date reconciliation
        this.nonSelfCommentCount  = other.nonSelfCommentCount;
        this.isUpdated = other.isUpdated;
    }

    public boolean isEmpty() {
        return events.isEmpty() && comments.isEmpty();
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

    public int getNonSelfCommentCount() { return nonSelfCommentCount; }

    public boolean isUpdated() { return isUpdated; }

    @Override
    public String toString() {
        return "Events: " + events.toString() + ", " + "comments: " + comments.toString();
    }
}
