package backend.json;

import backend.interfaces.RepoStore;
import backend.interfaces.StoreTask;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

class DeleteTask extends StoreTask {
    public final CompletableFuture<Boolean> response;

    protected DeleteTask(String repoId, CompletableFuture<Boolean> response) {
        super(repoId);
        this.response = response;
    }

    @Override
    public void run() {
        response.complete(RepoStore.delete(repoId));
    }
}
