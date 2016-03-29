package backend.control.operations;

import static util.Futures.chain;

import java.util.concurrent.CompletableFuture;

import backend.RepoIO;

public class RemoveRepoOp implements RepoOp<Boolean> {

    private final String repoId;
    private final RepoIO repoIO;
    private final CompletableFuture<Boolean> result;

    public RemoveRepoOp(String repoId, RepoIO repoIO, CompletableFuture<Boolean> result) {
        this.repoId = repoId;
        this.repoIO = repoIO;
        this.result = result;
    }

    @Override
    public String repoId() {
        return repoId;
    }

    @Override
    public CompletableFuture<Boolean> perform() {
        return repoIO.removeRepository(repoId)
                .thenApply(chain(result));
    }
}
