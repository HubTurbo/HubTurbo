package backend.control.operations;

import static util.Futures.chain;

import java.util.concurrent.CompletableFuture;

import backend.RepoIO;
import backend.resource.Model;

public class OpenRepoOp implements RepoOp<Model> {

    private final String repoId;
    private final RepoIO repoIO;
    private final CompletableFuture<Model> result;

    public OpenRepoOp(String repoId, RepoIO repoIO, CompletableFuture<Model> result) {
        this.repoId = repoId;
        this.repoIO = repoIO;
        this.result = result;
    }

    @Override
    public String repoId() {
        return repoId;
    }

    @Override
    public CompletableFuture<Model> perform() {
        return repoIO.openRepository(repoId)
                .thenApply(chain(result));
    }
}
