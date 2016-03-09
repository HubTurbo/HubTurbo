package backend.control.operations;

import backend.resource.MultiModel;
import backend.resource.TurboIssue;
import org.apache.logging.log4j.Logger;
import util.HTLog;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * This class represents a mutually exclusive operation to replace an issue's labels locally
 */
public class ReplaceIssueLabelsLocallyOp implements RepoOp<Optional<TurboIssue>> {
    private MultiModel models;
    private TurboIssue issue;
    private List<String> newLabels;
    private CompletableFuture<Optional<TurboIssue>> result;

    private static final Logger logger = HTLog.get(UpdateLocalModelOp.class);

    public ReplaceIssueLabelsLocallyOp(MultiModel models, TurboIssue issue, List<String> newlabels,
                                       CompletableFuture<Optional<TurboIssue>> result) {
        this.models = models;
        this.issue = issue;
        this.newLabels = newlabels;
        this.result = result;
    }

    @Override
    public String repoId() {
        return issue.getRepoId();
    }

    @Override
    public CompletableFuture<Optional<TurboIssue>> perform() {
        logger.info("Replacing labels for issue " + issue + " locally");
        Optional<TurboIssue> localReplaceResult =
                models.replaceIssueLabels(issue.getRepoId(), issue.getId(), newLabels);
        result.complete(localReplaceResult);
        return result;
    }
}
