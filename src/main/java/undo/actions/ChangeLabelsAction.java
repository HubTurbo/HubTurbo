package undo.actions;

import backend.resource.TurboIssue;
import javafx.util.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class ChangeLabelsAction implements Action<TurboIssue> {

    public static final String DESCRIPTION = "change label(s)";

    private final TurboIssue originalIssue;
    private final Set<String> addedLabels;
    private final Set<String> removedLabels;

    // Represents changes to the labels of a TurboIssue
    public ChangeLabelsAction(TurboIssue issue, List<String> addedLabels, List<String> removedLabels) {
        originalIssue = new TurboIssue(issue);
        this.addedLabels = new HashSet<>(addedLabels);
        this.removedLabels = new HashSet<>(removedLabels);
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    // Adds the labels to be added and removes the labels to be removed
    @Override
    public TurboIssue act(TurboIssue issue) {
        Set<String> newLabels = new TreeSet<>(issue.getLabels());
        newLabels.addAll(addedLabels);
        newLabels.removeAll(removedLabels);
        issue.setLabels(newLabels.stream().collect(Collectors.toList()));
        return issue;
    }

    // Adds the labels to be removed and removes the labels to be added
    @Override
    public TurboIssue undo(TurboIssue issue) {
        Set<String> newLabels = new TreeSet<>(issue.getLabels());
        newLabels.addAll(removedLabels);
        newLabels.removeAll(addedLabels);
        issue.setLabels(newLabels.stream().collect(Collectors.toList()));
        return issue;
    }

    /**
     * Reconciles two ChangeLabelActions
     *
     * e.g.
     * ChangeLabelAction a: +a +b +c
     * ChangeLabelAction b: -a +d

     * After reconciliation:
     * ReconciledAction A: +b +c (this gets sent off to GitHub)
     * ReconciledAction B: +d (this remains in the queue)
     * Undoing an action now, will only undo ReconciledAction B, which means only "+d" will be removed.
     * "-a" will not be undone.
     */
    @Override
    public Pair<Action, Action> reconcile(Action a, Action b) {
        // Ensure that actions are ChangeLabelActions
        if (a.getClass().equals(this.getClass()) && b.getClass().equals(this.getClass())) {
            ChangeLabelsAction actionA = (ChangeLabelsAction) a;
            ChangeLabelsAction actionB = (ChangeLabelsAction) b;
            // Ensure that both ChangeLabelActions apply to the same issue in the same repo
            if (actionA.getOriginalIssue().getRepoId().equals(actionB.getOriginalIssue().getRepoId()) &&
                    actionA.getOriginalIssue().getId() == actionB.getOriginalIssue().getId()) {
                // ReconciledActions: removes matching pairs of added and removed labels from actionA and actionB
                // ReconciledAction B's TurboIssue has ReconciledAction A applied to it so that its state matches
                // that on GitHub. 
                ChangeLabelsAction reconciledActionA = new ChangeLabelsAction(
                        actionA.getOriginalIssue(),
                        actionA.getAddedLabels().stream()
                                .filter(addedLabel -> !actionB.getRemovedLabels().contains(addedLabel))
                                .collect(Collectors.toList()),
                        actionA.getRemovedLabels().stream()
                                .filter(removedLabel -> !actionB.getAddedLabels().contains(removedLabel))
                                .collect(Collectors.toList()));
                ChangeLabelsAction reconciledActionB = new ChangeLabelsAction(
                        reconciledActionA.act(actionA.getOriginalIssue()),
                        actionB.getAddedLabels().stream()
                                .filter(addedLabel -> !actionA.getRemovedLabels().contains(addedLabel))
                                .collect(Collectors.toList()),
                        actionB.getRemovedLabels().stream()
                                .filter(removedLabel -> !actionA.getAddedLabels().contains(removedLabel))
                                .collect(Collectors.toList()));
                return new Pair<>(reconciledActionA, reconciledActionB);
            }
        }
        return new Pair<>(a, b);
    }

    // A no-op is when there are no labels to be added or removed
    @Override
    public boolean isNoOp() {
        return addedLabels.isEmpty() && removedLabels.isEmpty();
    }

    public Set<String> getAddedLabels() {
        return addedLabels;
    }

    public Set<String> getRemovedLabels() {
        return removedLabels;
    }

    public TurboIssue getOriginalIssue() {
        return originalIssue;
    }

    // Compares original labels of the TurboIssue with the list of new labels to get the lists of the
    // added and removed labels
    public static ChangeLabelsAction createChangeLabelsAction(TurboIssue issue, List<String> newLabels) {
        return new ChangeLabelsAction(issue,
                newLabels.stream()
                        .filter(newLabel -> !issue.getLabels().contains(newLabel))
                        .collect(Collectors.toList()),
                issue.getLabels().stream()
                        .filter(originalLabel -> !newLabels.contains(originalLabel))
                        .collect(Collectors.toList()));
    }

}
