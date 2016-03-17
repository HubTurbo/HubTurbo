package backend.control.operations;

import backend.resource.MultiModel;
import backend.resource.TurboIssue;
import org.apache.logging.log4j.Logger;
import util.HTLog;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * This class represents a mutually exclusive operation to replace an issue's assignee locally
 */
public class ReplaceIssueAssigneeLocallyOp implements RepoOp<Optional<TurboIssue>> {

    private final MultiModel models;
    private final TurboIssue issue;
    private final String assigneeLoginName;
    private final CompletableFuture<Optional<TurboIssue>> result;

    private static final Logger logger = HTLog.get(UpdateLocalModelOp.class);

    public ReplaceIssueAssigneeLocallyOp(MultiModel models, TurboIssue issue, String assigneeLoginName,
                                       CompletableFuture<Optional<TurboIssue>> result) {
        this.models = models;
        this.issue = issue;
        this.assigneeLoginName = assigneeLoginName;
        this.result = result;
    }

    @Override
    public String repoId() {
        return issue.getRepoId();
    }

    @Override
    public CompletableFuture<Optional<TurboIssue>> perform() {
        logger.info("Replacing assignee for issue " + issue + " locally");
        Optional<TurboIssue> localReplaceResult =
                models.replaceIssueAssignee(issue.getRepoId(), issue.getId(), assigneeLoginName);
        result.complete(localReplaceResult);
        return result;
    }

}
