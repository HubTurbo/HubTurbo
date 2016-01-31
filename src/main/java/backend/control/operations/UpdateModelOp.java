package backend.control.operations;

import static util.Futures.chain;

import java.util.concurrent.CompletableFuture;

import backend.RepoIO;
import backend.resource.Model;

public class UpdateModelOp implements RepoOp<Model> {

    private final Model oldModel;
    private final RepoIO repoIO;
    private final CompletableFuture<Model> result;

    public UpdateModelOp(Model oldModel, RepoIO repoIO, CompletableFuture<Model> result) {
        this.oldModel = oldModel;
        this.repoIO = repoIO;
        this.result = result;
    }

    @Override
    public String repoId() {
        return oldModel.getRepoId();
    }

    @Override
    public CompletableFuture<Model> perform() {
        return repoIO.updateModel(oldModel)
            .thenApply(chain(result));
    }
}
