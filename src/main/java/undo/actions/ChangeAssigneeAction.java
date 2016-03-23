package undo.actions;

import backend.Logic;
import backend.resource.TurboIssue;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ChangeAssigneeAction implements Action<TurboIssue> {
    public static final String DESCRIPTION = "Change assignee";

    private final Optional<String> oldAssigneeLoginName;
    private final Optional<String> newAssigneeLoginName;
    private final Logic logic;

    public ChangeAssigneeAction(Logic logic, Optional<String> oldAssigneeLoginName,
                                Optional<String> newAssigneeLoginName) {
        this.oldAssigneeLoginName = oldAssigneeLoginName;
        this.newAssigneeLoginName = newAssigneeLoginName;
        this.logic = logic;
    }

    public String getDescription() {
        return DESCRIPTION;
    }

    public CompletableFuture<Boolean> act(TurboIssue issue) {
        return logic.replaceIssueAssignee(issue, newAssigneeLoginName);
    }

    public CompletableFuture<Boolean> undo(TurboIssue issue) {
        return logic.replaceIssueAssignee(issue, oldAssigneeLoginName);
    }

}
