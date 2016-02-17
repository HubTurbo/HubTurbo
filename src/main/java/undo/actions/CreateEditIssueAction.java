package undo.actions;

import java.util.concurrent.CompletableFuture;

import backend.Logic;
import backend.resource.TurboIssue;

/**
 * Stores information about a TurboIssue before it is being edited.
 * Also responsible to delete previously newly created TurboIssue 
 */
public class CreateEditIssueAction implements Action<TurboIssue> {

    public static final String DESCRIPTION = "%s issue";

    private final Logic logic;
    private final TurboIssue oldIssue;
    private final TurboIssue newIssue;

    public CreateEditIssueAction(Logic logic, final TurboIssue oldIssue, final TurboIssue newIssue) {
        this.logic = logic;
        this.oldIssue = oldIssue;
        this.newIssue = newIssue;
    }

    @Override
    public String getDescription() {
        return String.format(DESCRIPTION, TurboIssue.isNewIssue(oldIssue) 
            ? "Create" : "Edit");
    }

    /**
     * Creates a new issue or edits an existing issue
     */
    @Override
    public CompletableFuture<Boolean> act(TurboIssue issue) {
        return logic.createIssue(newIssue.getRepoId(), newIssue);
    }

    /**
     * Replaces content of an exsiting issue with its previous version
     */
    @Override
    public CompletableFuture<Boolean> undo(TurboIssue issue) {
        // Cannot undo a newly created issue
        if (TurboIssue.isNewIssue(issue)) return CompletableFuture.completedFuture(false);
        return logic.createIssue(oldIssue.getRepoId(), oldIssue);

    }

}
