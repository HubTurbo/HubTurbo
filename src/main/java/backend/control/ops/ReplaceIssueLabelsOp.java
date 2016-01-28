package backend.control.ops;

import backend.RepoIO;
import backend.resource.TurboIssue;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static util.Futures.chain;

public class ReplaceIssueLabelsOp implements RepoOp<List<String>> {

    private final RepoIO repoIO;
    private final TurboIssue issue;
    private final List<String> labels;
    private final CompletableFuture<List<String>> result;

    public ReplaceIssueLabelsOp(RepoIO repoIO, CompletableFuture<List<String>> result,
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
    public CompletableFuture<List<String>> perform() {
        return repoIO.replaceIssueLabels(issue, labels)
                .thenApply(chain(result));
    }
}
