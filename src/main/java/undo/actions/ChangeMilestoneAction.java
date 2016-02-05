package undo.actions;

import backend.Logic;
import backend.resource.TurboIssue;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ChangeMilestoneAction implements Action<TurboIssue> {
    public static final String DESCRIPTION = "Change milestone";

    private Integer oldMilestoneValue;
    private Integer newMilestoneValue;
    private Logic logic;

    public ChangeMilestoneAction(Logic logic, Optional<Integer> oldMilestone, Optional<Integer> newMilestone) {
        this.oldMilestoneValue = (oldMilestone.isPresent()) ? oldMilestone.get() : null;
        this.newMilestoneValue = (newMilestone.isPresent()) ? newMilestone.get() : null;
        this.logic = logic;
    }

    public String getDescription() {
        return DESCRIPTION;
    }

    public CompletableFuture<Boolean> act(TurboIssue issue) {
        return logic.replaceIssueMilestone(issue, newMilestoneValue);
    }

    public CompletableFuture<Boolean> undo(TurboIssue issue) {
        return logic.replaceIssueMilestone(issue, oldMilestoneValue);
    }

}
