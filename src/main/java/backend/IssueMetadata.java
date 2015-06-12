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

    // Constructor for default use when initializing TurboIssue
    public IssueMetadata() {
        events = new ArrayList<>();
        comments = new ArrayList<>();
        nonSelfUpdatedAt = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.ofHours(0));
    }

    // Constructor used in DownloadMetadataTask
    public IssueMetadata(List<TurboIssueEvent> events, List<Comment> comments) {
        this.events = events;
        this.comments = comments;
        this.nonSelfUpdatedAt = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.ofHours(0)); // Not calculated yet
    }

    // Constructor used in Logic
    public IssueMetadata(IssueMetadata other, LocalDateTime lastNonSelfUpdatedAt) {
        this.events = new ArrayList<>(other.events);
        this.comments = new ArrayList<>(other.comments);
        this.nonSelfUpdatedAt = lastNonSelfUpdatedAt; // Calculated just prior to calling this constructor
    }

    // Constructor used in MultiModel
    public IssueMetadata(IssueMetadata other) {
        this.events = new ArrayList<>(other.events);
        this.comments = new ArrayList<>(other.comments);
        this.nonSelfUpdatedAt = other.nonSelfUpdatedAt;
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

    @Override
    public String toString() {
        return "Events: " + events.toString() + ", " + "comments: " + comments.toString();
    }
}
