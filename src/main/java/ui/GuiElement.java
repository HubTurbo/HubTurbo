package ui;

import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import backend.resource.TurboMilestone;
import backend.resource.TurboUser;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GuiElement {
    private TurboIssue issue;
    private List<TurboLabel> labels;
    private Optional<TurboMilestone> milestone;
    private Optional<TurboUser> assignee;

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
     * Returns all labels related to an issue (including labels that have been added and then removed).
     * Use getLabelsOfIssue to retrieve an issue's current labels instead.
     *
     * @return Labels related to an issue, or null if the GuiElement has been constructed as such.
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

    public List<TurboLabel> getLabelsOfIssue(TurboIssue issue) {
        return issue.getLabels().stream().map(labelName -> {
            Optional<TurboLabel> label = getLabelByActualName(labelName); // O(n^2) for the additional assert.
            assert label.isPresent();
            return label.get();
        }).collect(Collectors.toList());
    }
}
