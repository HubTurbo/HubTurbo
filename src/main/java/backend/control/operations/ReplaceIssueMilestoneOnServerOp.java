package backend.control.operations;

import backend.RepoIO;
import backend.resource.TurboIssue;
import org.eclipse.egit.github.core.Issue;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static util.Futures.chain;

public class ReplaceIssueMilestoneOnServerOp implements RepoOp<Boolean> {
    private final RepoIO repoIO;
    private final TurboIssue issue;
    private final Optional<Integer> milestone;
    private final CompletableFuture<Boolean> result;

    public ReplaceIssueMilestoneOnServerOp(RepoIO repoIO, CompletableFuture<Boolean> result,
                                           TurboIssue issue, Optional<Integer> milestone) {
        this.repoIO = repoIO;
        this.result = result;

        this.issue = issue;
        this.milestone = milestone;
    }

    @Override
    public String repoId() {
        return issue.getRepoId();
    }

    @Override
    public CompletableFuture<Boolean> perform() {
        return repoIO.replaceIssueMilestone(issue, milestone)
                .thenApply(chain(result));
    }
}
