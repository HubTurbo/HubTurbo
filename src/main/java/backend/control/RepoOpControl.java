package backend.control;

import backend.RepoIO;
import backend.control.operations.*;
import backend.github.GitHubModelUpdatesData;
import backend.resource.Model;
import backend.resource.MultiModel;
import backend.resource.TurboIssue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;


/**
 * A means of repo-level synchronisation for select RepoIO operations. Only one instance of this class
 * is available at any time availabel through {@code getRepoOpControl}. A new instance can be created with
 * {@code createRepoOpControl} and will replace any existing instance
 */
public final class RepoOpControl {

    private static final Logger logger = LogManager.getLogger(RepoOpControl.class.getName());

    private final RepoIO repoIO;
    private final MultiModel models;

    private final ExecutorService pool = Executors.newCachedThreadPool();
    private final Map<String, BlockingQueue<RepoOp>> queues = new HashMap<>();

    public RepoOpControl(RepoIO repoIO, MultiModel models) {
        this.repoIO = repoIO;
        this.models = models;
    }

    public CompletableFuture<Model> openRepository(String repoId) {
        init(repoId);
        CompletableFuture<Model> result = new CompletableFuture<>();
        enqueue(new OpenRepoOp(repoId, repoIO, result));
        return result;
    }

    /**
     * Updates repository stored locally with data from a GitHubModelUpdatesData object.
     * Set syncOperation to queue this operation in the blocking queue for the updating repository
     * @param updates
     * @param syncOperation
     * @return
     */
    public CompletableFuture<Model> updateLocalModel(GitHubModelUpdatesData updates,
                                                     boolean syncOperation) {
        init(updates.getRepoId());
        CompletableFuture<Model> result = new CompletableFuture<>();
        UpdateLocalModelOp op = new UpdateLocalModelOp(models, updates, result);
        if (syncOperation) {
            enqueue(new UpdateLocalModelOp(models, updates, result));
        } else {
            op.perform();
        }
        return result;
    }

    public CompletableFuture<Boolean> removeRepository(String repoId) {
        init(repoId);
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        enqueue(new RemoveRepoOp(repoId, repoIO, result));
        return result;
    }

    public CompletableFuture<Boolean> replaceIssueLabelsOnServer(TurboIssue issue, List<String> labels) {
        init(issue.getRepoId());
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        enqueue(new ReplaceIssueLabelsOnServerOp(repoIO, result, issue, labels));
        return result;
    }

    public CompletableFuture<Optional<TurboIssue>> replaceIssueLabelsLocally(TurboIssue issue, List<String> labels) {
        init(issue.getRepoId());
        CompletableFuture<Optional<TurboIssue>> result = new CompletableFuture<>();
        enqueue(new ReplaceIssueLabelsLocallyOp(models, issue, labels, result));
        return result;
    }

    /**
     * Ensures that repo-specific state is initialised. Should be called
     * at the start of each operation method.
     */
    private void init(String repoId) {
        boolean isNewRepo = !queues.containsKey(repoId);
        if (isNewRepo) {
            // We want an unbounded deque so enqueueing will never block
            // and can flexibly add and remove operations.
            queues.put(repoId, new LinkedBlockingDeque<>());
            pool.execute(() -> dequeue(repoId));
        }
    }

    /**
     * Enqueues an operation to be handled later. Operations on the same repo
     * (and in the same queue) are guaranteed to be handled by the same thread.
     */
    private void enqueue(RepoOp op) {
        BlockingQueue<RepoOp> q = queues.get(op.repoId());

        // TODO scan queue for patterns

        try {
            q.put(op);
        } catch (InterruptedException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Dequeues an operation to handle it. This method may block in two scenarios:
     * if the queue is empty (in which case take blocks) or when waiting for a task
     * (in which case get blocks).
     */
    private void dequeue(String repoId) {
        BlockingQueue<RepoOp> q = queues.get(repoId);

        while (true) {
            try {
                q.take().perform().get();
            } catch (ExecutionException | InterruptedException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }
    }
}
