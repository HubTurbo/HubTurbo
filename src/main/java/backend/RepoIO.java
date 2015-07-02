package backend;

import static util.Futures.withResult;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.Logger;

import ui.UI;
import util.HTLog;
import backend.github.GitHubSource;
import backend.interfaces.RepoSource;
import backend.interfaces.RepoStore;
import backend.json.JSONStore;
import backend.json.JSONStoreStub;
import backend.resource.Model;
import backend.resource.serialization.SerializableModel;
import backend.stub.DummySource;

public class RepoIO {

    private static final Logger logger = HTLog.get(RepoIO.class);

    private final RepoSource repoSource;
    private final RepoStore repoStore;

    public RepoIO(boolean isTestMode, boolean enableTestJSON) {
        if (isTestMode && !enableTestJSON) {
            repoStore = new JSONStoreStub();
        } else {
            repoStore = new JSONStore();
        }

        if (isTestMode) {
            repoSource = new DummySource();
            RepoStore.enableTestDirectory();
        } else {
            repoSource = new GitHubSource();
        }
    }

    public CompletableFuture<Boolean> login(UserCredentials credentials) {
        return repoSource.login(credentials);
    }

    public CompletableFuture<Boolean> isRepositoryValid(String repoId) {
        return repoSource.isRepositoryValid(repoId);
    }

    public CompletableFuture<Model> openRepository(String repoId) {
        if (repoStore.isRepoStored(repoId)) {
            return loadRepoFromStoreAsync(repoId)
                    .exceptionally(e -> {
                        return downloadRepoFromSourceBlocking(repoId);
                    });
        } else {
            return downloadRepoFromSourceAsync(repoId);
        }
    }

    private CompletableFuture<Model> loadRepoFromStoreAsync(String repoId) {
        return repoStore.loadRepository(repoId)
                .thenCompose(this::updateModel);
    }

    private CompletableFuture<Model> downloadRepoFromSourceAsync(String repoId) {
        UI.status.displayMessage("Downloading " + repoId);
        return repoSource.downloadRepository(repoId)
                .thenCompose(this::updateModel)
                .exceptionally(withResult(new Model(repoId)));
    }

    private Model downloadRepoFromSourceBlocking(String repoId) {
        try {
            return downloadRepoFromSourceAsync(repoId).get();
        } catch (ExecutionException | InterruptedException e) {
            logger.info("Error while downloading " + repoId);
            logger.error(e.getLocalizedMessage());

            return new Model(repoId);
        }
    }

    public CompletableFuture<Model> updateModel(Model model) {
        return repoSource.updateModel(model)
            .thenApply(newModel -> {
                UI.status.displayMessage(model.getRepoId() + " is up to date!");
                if (!model.equals(newModel)) {
                    repoStore.saveRepository(newModel.getRepoId(), new SerializableModel(newModel));
                } else {
                    logger.info(HTLog.format(model.getRepoId(),
                        "Nothing changed; not writing to store"));
                }
                return newModel;
            }).exceptionally(withResult(new Model(model.getRepoId())));
    }

    public CompletableFuture<Map<Integer, IssueMetadata>> getIssueMetadata(String repoId, List<Integer> issues) {
        return repoSource.downloadMetadata(repoId, issues);
    }
}
