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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.Logger;
import ui.UI;
import util.HTLog;
import util.events.ShowErrorDialogEvent;
import util.events.UpdateProgressEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static util.Futures.withResult;

public class RepoIO {

    private static final Logger logger = HTLog.get(RepoIO.class);

    private final RepoSource repoSource;
    private final JSONStore jsonStore;

    private List<String> storedRepos;

    private static final int MAX_REDOWNLOAD_TRIES = 2;

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
        // The ignoreCase logic is necessary when we are opening a repo from the login dialog window
        // i.e. when the isAlreadyOpen check in Logic fails.
        Optional<String> matchingRepoName = storedRepos.stream().filter(repoName ->
                repoName.equalsIgnoreCase(repoId)).findFirst();
        if (matchingRepoName.isPresent()) {
            // TODO avoid CI deadlock in the .exceptionally call. Explanation:
            /* loadRepoFromStoreAsync will execute in jsonStore's single thread pool, and if
             it has an exception then downloadRepoFromSourceBlocking will also run there. Eventually,
             this results in jsonStore.saveRepository in updateModel being placed as another Task on the
             same thread pool. However, since the current task is still carrying out and waiting for the second
             task to complete, the program gets deadlocked on the CI.
             One example of how this can happen is when storedRepos contains the repo name but the json was
             deleted while the program is still running. */
            String repoToLoad = matchingRepoName.get();
            return loadRepoFromStoreAsync(repoToLoad)
                    .exceptionally(e -> downloadRepoFromSourceBlocking(repoToLoad));
        } else {
            return downloadRepoFromSourceAsync(repoId);
        }
    }

    private CompletableFuture<Model> loadRepoFromStoreAsync(String repoId) {
        return jsonStore.loadRepository(repoId)
                .thenCompose(this::updateModel);
    }

    private CompletableFuture<Model> downloadRepoFromSourceAsync(String repoID) {
        return downloadRepoFromSourceAsync(repoID, MAX_REDOWNLOAD_TRIES);
    }

    private CompletableFuture<Model> downloadRepoFromSourceAsync(String repoId, int remainingTries) {
        UI.status.displayMessage("Downloading " + repoId);
        return repoSource.downloadRepository(repoId)
                .thenCompose(newModel -> updateModel(newModel, remainingTries))
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
        return updateModel(model, MAX_REDOWNLOAD_TRIES);
    }

    public CompletableFuture<Model> updateModel(Model model, int remainingTries) {
        return repoSource.updateModel(model)
            .thenApply(newModel -> {
                boolean corruptedJson = false;
                if (!model.equals(newModel)) {
                    try {
                        corruptedJson =
                                jsonStore.saveRepository(newModel.getRepoId(), new SerializableModel(newModel)).get();
                    } catch (InterruptedException | ExecutionException ex) {
                        corruptedJson = true;
                    }
                } else {
                    logger.info(HTLog.format(model.getRepoId(),
                            "Nothing changed; not writing to store"));
                }
                if (corruptedJson && remainingTries > 0) {
                    return downloadRepoFromSourceAsync(model.getRepoId(), remainingTries - 1).join();
                } else {
                    if (corruptedJson && remainingTries == 0) {
                        UI.events.triggerEvent(new ShowErrorDialogEvent("Could not sync " + model.getRepoId(),
                                "We were not able to sync with GitHub to retrieve and store data for the repository "
                                + model.getRepoId()
                                + ". Please let us know if you encounter this issue consistently."
                        ));
                    } else {
                        UI.status.displayMessage(model.getRepoId() + " is up to date!");
                    }
                    UI.events.triggerEvent(new UpdateProgressEvent(model.getRepoId()));
                    return newModel;
                }
            }).exceptionally(withResult(new Model(model.getRepoId())));
    }

    public CompletableFuture<Map<Integer, IssueMetadata>> getIssueMetadata(String repoId, List<Integer> issues) {
        return repoSource.downloadMetadata(repoId, issues);
    }

    public CompletableFuture<List<String>> replaceIssueLabels(TurboIssue issue, List<String> labels) {
        return repoSource.replaceIssueLabels(issue, labels);
    }

    public CompletableFuture<ImmutablePair<Integer, Long>> getRateLimitResetTime() {
        return repoSource.getRateLimitResetTime();
    }

}
