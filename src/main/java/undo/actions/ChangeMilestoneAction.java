package undo.actions;

import backend.Logic;
import backend.resource.TurboIssue;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ChangeMilestoneAction implements Action<TurboIssue> {
    public static final String DESCRIPTION = "Change milestone";

    private final Optional<Integer> oldMilestone;
    private final Optional<Integer> newMilestone;
    private final Logic logic;

    public ChangeMilestoneAction(Logic logic, Optional<Integer> oldMilestone, Optional<Integer> newMilestone) {
        this.oldMilestone = oldMilestone;
        this.newMilestone = newMilestone;
        this.logic = logic;
    }

    public String getDescription() {
        return DESCRIPTION;
    }

    public CompletableFuture<Boolean> act(TurboIssue issue) {
        return logic.replaceIssueMilestone(issue, newMilestone);
    }

    public CompletableFuture<Boolean> undo(TurboIssue issue) {
        return logic.replaceIssueMilestone(issue, oldMilestone);
    }

}
