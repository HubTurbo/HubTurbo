package backend.control.operations;

import backend.RepoIO;
import backend.resource.TurboIssue;

import java.util.concurrent.CompletableFuture;

import static util.Futures.chain;

public class ReplaceIssueAssigneeOp implements RepoOp<Boolean> {
    private final RepoIO repoIO;
    private final TurboIssue issue;
    private final String assigneeLoginName;
    private final CompletableFuture<Boolean> result;

    public ReplaceIssueAssigneeOp(RepoIO repoIO, CompletableFuture<Boolean> result,
                                   TurboIssue issue, String assigneeLoginName) {
        this.repoIO = repoIO;
        this.result = result;

        this.issue = issue;
        this.assigneeLoginName = assigneeLoginName;
    }

    @Override
    public String repoId() {
        return issue.getRepoId();
    }

    @Override
    public CompletableFuture<Boolean> perform() {
        return repoIO.replaceIssueAssignee(issue, assigneeLoginName)
                .thenApply(chain(result));
    }
}
