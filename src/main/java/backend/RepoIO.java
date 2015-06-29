package backend;

import backend.github.GitHubSource;
import backend.interfaces.RepoSource;
import backend.interfaces.RepoStore;
import backend.json.JSONStore;
import backend.json.JSONStoreStub;
import backend.resource.Model;
import backend.resource.serialization.SerializableModel;
import backend.stub.DummySource;
import org.apache.logging.log4j.Logger;
import ui.UI;
import util.HTLog;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static util.Futures.withResult;

public class RepoIO {

    private static final Logger logger = HTLog.get(RepoIO.class);

    private final RepoSource repoSource;
    private final JSONStore jsonStore;

    private List<String> storedRepos;

    public RepoIO(boolean isTestMode, boolean enableTestJSON) {
        if (isTestMode && !enableTestJSON) {
            jsonStore = new JSONStoreStub();
        } else {
            jsonStore = new JSONStore();
            storedRepos = new ArrayList<>(jsonStore.getStoredRepos());
        }

        if (isTestMode) {
            repoSource = new DummySource();
            RepoStore.enableTestDirectory();
        } else {
            repoSource = new GitHubSource();
        }
    }

    public List<String> getStoredRepos() {
        return storedRepos;
    }

    public CompletableFuture<Boolean> login(UserCredentials credentials) {
        return repoSource.login(credentials);
    }

    public CompletableFuture<Boolean> isRepositoryValid(String repoId) {
        return repoSource.isRepositoryValid(repoId);
    }

    public CompletableFuture<Model> openRepository(String repoId) {
        if (storedRepos.contains(repoId)) {
            return loadRepositoryFromStore(repoId);
        } else {
            return downloadRepositoryFromSource(repoId);
        }
    }

    private CompletableFuture<Model> loadRepositoryFromStore(String repoId) {
        return jsonStore.loadRepository(repoId)
                .thenCompose(this::updateModel)
                .exceptionally(ex -> {
                    try {
                        return downloadRepositoryFromSource(repoId).get();
                    } catch (ExecutionException | InterruptedException e) {
                        logger.info("Error while downloading " + repoId);
                        logger.error(e.getLocalizedMessage());

                        return new Model(repoId);
                    }
                });
    }

    private CompletableFuture<Model> downloadRepositoryFromSource(String repoId) {
        storedRepos.add(repoId);
        UI.status.displayMessage("Downloading " + repoId);
        return repoSource.downloadRepository(repoId)
                .thenCompose(this::updateModel)
                .exceptionally(withResult(new Model(repoId)));
    }

    public CompletableFuture<Model> updateModel(Model model) {
        return repoSource.updateModel(model)
            .thenApply(newModel -> {
                UI.status.displayMessage(model.getRepoId() + " is up to date!");
                if (!model.equals(newModel)) {
                    jsonStore.saveRepository(newModel.getRepoId(), new SerializableModel(newModel));
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
