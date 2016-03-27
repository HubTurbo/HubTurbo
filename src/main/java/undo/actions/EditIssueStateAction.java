package undo.actions;

import backend.Logic;
import backend.resource.TurboIssue;

import java.util.concurrent.CompletableFuture;

/**
 * Close or reopen an issue.
 */
public class EditIssueStateAction implements Action<TurboIssue> {

    private static final String DESCRIPTION_REOPENED = "Reopened";
    private static final String DESCRIPTION_CLOSED = "Closed";
    private final Logic logic;
    private final boolean isOpen;

    /**
     * Take in the new state, as well as the logic used to update the state.
     *
     * @param logic
     * @param isOpen Whether we want to set the new state to open or not
     */
    public EditIssueStateAction(Logic logic, boolean isOpen) {
        this.logic = logic;
        this.isOpen = isOpen;
    }

    @Override
    public String getDescription() {
        return isOpen ? DESCRIPTION_REOPENED : DESCRIPTION_CLOSED;
    }

    /**
     * Update the issue to a new state. Used for either closing or reopening an issue.
     *
     * @param issue The TurboIssue to be acted on
     * @return The result of state editing.
     */
    @Override
    public CompletableFuture<Boolean> act(TurboIssue issue) {
        return logic.editIssueState(issue, isOpen);
    }

    /**
     * Undo an edit in the state of the issue.
     *
     * @param issue The TueboIssue to be acted on
     * @return The result of state editing.
     */
    @Override
    public CompletableFuture<Boolean> undo(TurboIssue issue) {
        return logic.editIssueState(issue, !isOpen);
    }
}
