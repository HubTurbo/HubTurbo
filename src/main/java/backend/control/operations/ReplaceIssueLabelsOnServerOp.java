package backend.control.operations;

import backend.RepoIO;
import backend.resource.TurboIssue;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static util.Futures.chain;

/**
 * This class represents a repository operation that replaces a list of labels assigned to an issue
 */
public class ReplaceIssueLabelsOnServerOp implements RepoOp<Boolean> {

    private final RepoIO repoIO;
    private final TurboIssue issue;
    private final List<String> labels;
    private final CompletableFuture<Boolean> result;

    public ReplaceIssueLabelsOnServerOp(RepoIO repoIO, CompletableFuture<Boolean> result,
                                        TurboIssue issue, List<String> labels) {
        this.repoIO = repoIO;
        this.result = result;

        this.issue = issue;
        this.labels = labels;
    }

    @Override
    public String repoId() {
        return issue.getRepoId();
    }

    @Override
    public CompletableFuture<Boolean> perform() {
        return repoIO.replaceIssueLabels(issue, labels)
                .thenApply(chain(result));
    }
}
