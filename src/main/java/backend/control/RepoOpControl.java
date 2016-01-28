package backend.control;

import backend.RepoIO;
import backend.control.ops.*;
import backend.resource.Model;
import backend.resource.TurboIssue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;


/**
 * A means of repo-level synchronisation for select RepoIO operations.
 */
public class RepoOpControl {

    private static final Logger logger = LogManager.getLogger(RepoOpControl.class.getName());

    private final RepoIO repoIO;

    private final ExecutorService pool = Executors.newCachedThreadPool();
    private final Map<String, BlockingQueue<RepoOp>> queues = new HashMap<>();

    public RepoOpControl(RepoIO repoIO) {
        this.repoIO = repoIO;
    }

    /**
     * Wrapped operations. These methods correspond to RepoIO operations
     * which should not be executed concurrently. For example, we don't want
     * a model that is being updated to be deleted until the update is complete.
     */

    public CompletableFuture<Model> openRepository(String repoId) {
        init(repoId);
        CompletableFuture<Model> result = new CompletableFuture<>();
        enqueue(new OpenRepoOp(repoId, repoIO, result));
        return result;
    }

    public CompletableFuture<Boolean> removeRepository(String repoId) {
        init(repoId);
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        enqueue(new RemoveRepoOp(repoId, repoIO, result));
        return result;
    }

    public CompletableFuture<Model> updateModel(Model oldModel) {
        init(oldModel.getRepoId());
        CompletableFuture<Model> result = new CompletableFuture<>();
        enqueue(new UpdateModelOp(oldModel, repoIO, result));
        return result;
    }

    public CompletableFuture<List<String>> replaceIssueLabels(TurboIssue issue, List<String> labels) {
        init(issue.getRepoId());
        CompletableFuture<List<String>> result = new CompletableFuture<>();
        enqueue(new ReplaceIssueLabelsOp(repoIO, result, issue, labels));
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
