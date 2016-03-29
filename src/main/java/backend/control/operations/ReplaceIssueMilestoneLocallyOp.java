package backend.control.operations;

import backend.resource.MultiModel;
import backend.resource.TurboIssue;
import org.apache.logging.log4j.Logger;
import util.HTLog;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;


public class ReplaceIssueMilestoneLocallyOp implements RepoOp<Optional<TurboIssue>> {
    private final MultiModel models;
    private final TurboIssue issue;
    private final Optional<Integer> milestone;
    private final CompletableFuture<Optional<TurboIssue>> result;

    private static final Logger logger = HTLog.get(UpdateLocalModelOp.class);

    public ReplaceIssueMilestoneLocallyOp(MultiModel models, CompletableFuture<Optional<TurboIssue>> result,
                                          TurboIssue issue, Optional<Integer> milestone) {
        this.models = models;
        this.result = result;

        this.issue = issue;
        this.milestone = milestone;
    }

    @Override
    public String repoId() {
        return issue.getRepoId();
    }

    @Override
    public CompletableFuture<Optional<TurboIssue>> perform() {
        logger.info("Replacing milestone for issue " + issue + " locally");
        Optional<TurboIssue> localReplaceResult =
                models.replaceIssueMilestone(issue.getRepoId(), issue.getId(), milestone);
        result.complete(localReplaceResult);
        return result;
    }
}
