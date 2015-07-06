package backend;

import backend.github.GitHubSource;
import backend.interfaces.RepoSource;
import backend.interfaces.RepoStore;
import backend.json.JSONStore;
import backend.json.JSONStoreStub;
import backend.resource.Model;
import backend.resource.TurboIssue;
import backend.resource.serialization.SerializableModel;
import backend.stub.DummySource;
import org.apache.logging.log4j.Logger;
import ui.UI;
import util.Futures;
import util.HTLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static util.Futures.withResult;

public class RepoIO {

    private static final Logger logger = HTLog.get(RepoIO.class);

    private final RepoSource repoSource;
    private final JSONStore jsonStore;

    private List<String> storedRepos;

    public RepoIO(boolean isTestMode, boolean enableTestJSON) {
        if (isTestMode) {
            repoSource = new DummySource();
            RepoStore.enableTestDirectory();
        } else {
            repoSource = new GitHubSource();
        }
        if (isTestMode && !enableTestJSON) {
            jsonStore = new JSONStoreStub();
            storedRepos = new ArrayList<>();
        } else {
            jsonStore = new JSONStore();
            storedRepos = new ArrayList<>(jsonStore.getStoredRepos());
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
            return loadRepoFromStoreAsync(repoId)
                    .exceptionally(e -> downloadRepoFromSourceBlocking(repoId));
        } else {
            return downloadRepoFromSourceAsync(repoId);
        }
    }

    private CompletableFuture<Model> loadRepoFromStoreAsync(String repoId) {
        return jsonStore.loadRepository(repoId)
                .thenCompose(this::updateModel);
    }

    private CompletableFuture<Model> downloadRepoFromSourceAsync(String repoId) {
        UI.status.displayMessage("Downloading " + repoId);
        return repoSource.downloadRepository(repoId)
                .thenCompose(this::updateModel)
                .thenApply(model -> {
                    storedRepos.add(repoId);
                    return model;
                })
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

    public CompletableFuture<Boolean> replaceIssueLabels(TurboIssue issue, List<String> labels) {
        // will print out all new labels until this is properly implemented
        System.out.print("New Labels: ");
        labels.forEach(label -> System.out.print(label + " "));
        System.out.println();
        return Futures.unit(true);
    }

//    public CompletableFuture<List<String>> replaceIssueLabels(TurboIssue issue, List<String> labels) {
//        return repoSource.replaceIssueLabels(issue, labels);
//    }

}
