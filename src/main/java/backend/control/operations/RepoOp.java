package backend.control.operations;

import java.util.concurrent.CompletableFuture;

/**
 * A repository-level operation.
 *
 * @param <T> the return type of the operation
 */
public interface RepoOp<T> {
    /**
     * The repository that the operation acts on
     */
    String repoId();

    /**
     * The operation's implementation.
     */
    CompletableFuture<T> perform();
}
