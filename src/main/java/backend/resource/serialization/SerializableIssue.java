package backend.resource.serialization;

import backend.resource.TurboIssue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Warnings are suppressed to prevent complaints about fields not being final.
 * They are this way to give them default values.
 */
@SuppressWarnings("PMD")
public class SerializableIssue {
    private int id = 0;
    private String title = "";
    private String creator = "";
    private LocalDateTime createdAt = LocalDateTime.now();
    private boolean isPullRequest = false;
    private String description = "";
    private LocalDateTime updatedAt = LocalDateTime.now();
    private int commentCount = 0;
    private boolean isOpen = true;
    private Optional<String> assignee = Optional.empty();
    private List<String> labels = new ArrayList<>();
    private Optional<Integer> milestone = Optional.empty();

    public SerializableIssue(TurboIssue issue) {
        this.id = issue.getId();
        this.title = issue.getTitle();
        this.creator = issue.getCreator();
        this.createdAt = issue.getCreatedAt();
        this.isPullRequest = issue.isPullRequest();
        this.description = issue.getDescription();
        this.updatedAt = issue.getUpdatedAt();
        this.commentCount = issue.getCommentCount();
        this.isOpen = issue.isOpen();
        this.assignee = issue.getAssignee();
        this.labels = issue.getLabels();
        this.milestone = issue.getMilestone();
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCreator() {
        return creator;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isPullRequest() {
        return isPullRequest;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public Optional<String> getAssignee() {
        return assignee;
    }

    public List<String> getLabels() {
        return labels;
    }

    public Optional<Integer> getMilestone() {
        return milestone;
    }
}
