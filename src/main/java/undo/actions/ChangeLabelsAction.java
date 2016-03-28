package undo.actions;

import backend.Logic;
import backend.resource.TurboIssue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Holds both the original list and the new list of labels.
 */
public class ChangeLabelsAction implements Action<TurboIssue> {

    public static final String DESCRIPTION = "Label(s) changed";

    private final Logic logic;
    private final List<String> originalLabels;
    private final List<String> newLabels;

    /**
     * Takes in two lists of labels and the Logic used to replace the labels.
     *
     * @param originalLabels The original list of labels
     * @param newLabels      The new list of labels
     */
    public ChangeLabelsAction(Logic logic, List<String> originalLabels, List<String> newLabels) {
        this.logic = logic;
        this.originalLabels = new ArrayList<>(originalLabels);
        this.newLabels = new ArrayList<>(newLabels);
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    /**
     * Replaces the labels of the issue with the list of new labels.
     *
     * @param issue The TurboIssue to be acted on
     * @return The result of replacing the labels
     */
    @Override
    public CompletableFuture<Boolean> act(TurboIssue issue) {
        return logic.replaceIssueLabels(issue, newLabels);
    }

    /**
     * Replaces the labels of the issue with the original list of labels.
     *
     * @param issue The TurboIssue to be acted on
     * @return The result of replacing the labels
     */
    @Override
    public CompletableFuture<Boolean> undo(TurboIssue issue) {
        return logic.replaceIssueLabels(issue, originalLabels);
    }

}
