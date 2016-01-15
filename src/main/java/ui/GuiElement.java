package ui;

import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import backend.resource.TurboMilestone;
import backend.resource.TurboUser;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GuiElement {
    private final TurboIssue issue;
    private final List<TurboLabel> labels;
    private final Optional<TurboMilestone> milestone;
    private final Optional<TurboUser> assignee;

    public GuiElement(TurboIssue issue,
                      List<TurboLabel> labels,
                      Optional<TurboMilestone> milestone,
                      Optional<TurboUser> assignee) {
        this.issue = issue;
        this.labels = labels;
        this.milestone = milestone;
        this.assignee = assignee;
    }

    public TurboIssue getIssue() {
        return issue;
    }

    /**
     * Returns labels of the issue, not including those that have been removed from the issue, as they might
     * have been deleted from the repo altogether. Thus, this list is not used for displaying removing/adding-label
     * events.
     *
     * @return Current labels of the encapsulated issue, or an empty list if it doesn't have any labels currently.
     */
    public List<TurboLabel> getLabels() {
        return labels;
    }

    public Optional<TurboMilestone> getMilestone() {
        return milestone;
    }

    public Optional<TurboUser> getAssignee() {
        return assignee;
    }

    public Optional<TurboLabel> getLabelByActualName(String actualName) {
        return labels.stream().filter(label -> label.getActualName().equals(actualName)).findFirst();
    }
}
