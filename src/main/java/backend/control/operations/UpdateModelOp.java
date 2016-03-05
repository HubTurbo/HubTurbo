package backend.control.operations;

import backend.RepoIO;
import backend.resource.Model;

import java.util.concurrent.CompletableFuture;

import static util.Futures.chain;

/**
 * This class represents a mutually exclusive operation that injects
 * updated resources into repositories stored locally as Models
 */
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
