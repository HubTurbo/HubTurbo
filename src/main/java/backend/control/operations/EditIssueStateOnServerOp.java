package backend.control.operations;

import backend.RepoIO;
import backend.resource.TurboIssue;
import org.apache.logging.log4j.Logger;
import util.HTLog;

import java.util.concurrent.CompletableFuture;

import static util.Futures.chain;

/**
 * This class represents a repository operation that edit the open/closed state of an issue
 */
public class EditIssueStateOnServerOp implements RepoOp<Boolean> {

    private final RepoIO repoIO;
    private final TurboIssue issue;
    private final boolean isOpen;
    private final CompletableFuture<Boolean> result;

    private static final Logger logger = HTLog.get(EditIssueStateOnServerOp.class);

    public EditIssueStateOnServerOp(RepoIO repoIO, CompletableFuture<Boolean> result,
                                    TurboIssue issue, boolean isOpen) {
        this.repoIO = repoIO;
        this.issue = issue;
        this.isOpen = isOpen;
        this.result = result;
    }

    @Override
    public String repoId() {
        return issue.getRepoId();
    }

    @Override
    public CompletableFuture<Boolean> perform() {
        String state = isOpen ? "\"Open\"" : "\"Closed\"";
        logger.info("Editing state of " + issue + " to " + state + " on GitHub");
        return repoIO.editIssueState(issue, isOpen)
                .thenApply(chain(result));
    }
}
