package backend.control.operations;

import backend.resource.MultiModel;
import backend.resource.TurboIssue;
import org.apache.logging.log4j.Logger;
import util.HTLog;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * This class represents a mutually exclusive operation to edit an issue's state locally
 */
public class EditIssueStateLocallyOp implements RepoOp<Optional<TurboIssue>> {

    private final MultiModel models;
    private final TurboIssue issue;
    private final boolean isOpen;
    private final CompletableFuture<Optional<TurboIssue>> result;

    private static final Logger logger = HTLog.get(EditIssueStateLocallyOp.class);

    public EditIssueStateLocallyOp(MultiModel models, CompletableFuture<Optional<TurboIssue>> result,
                                   TurboIssue issue, boolean isOpen) {
        this.models = models;
        this.issue = issue;
        this.isOpen = isOpen;
        this.result = result;
    }

    @Override
    public String repoId() {
        return issue.getRepoId();
    }

    @Override
    public CompletableFuture<Optional<TurboIssue>> perform() {
        String state = isOpen ? "\"Open\"" : "\"Closed\"";
        logger.info("Editing state for issue " + issue + " to " + state + " locally");
        Optional<TurboIssue> localEditResult =
                models.editIssueState(repoId(), issue.getId(), isOpen);
        result.complete(localEditResult);
        return result;
    }
}
