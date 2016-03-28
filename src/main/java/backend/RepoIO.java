package backend;

import backend.control.RepoOpControl;
import backend.github.GitHubModelUpdatesData;
import backend.github.GitHubSource;
import backend.interfaces.RepoSource;
import backend.interfaces.RepoStore;
import backend.json.JSONStore;
import backend.resource.Model;
import backend.resource.TurboIssue;
import backend.resource.TurboMilestone;
import backend.resource.serialization.SerializableModel;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.Issue;
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

import static util.Futures.withResult;

public class RepoIO {

    private static final Logger logger = HTLog.get(RepoIO.class);

    private final RepoSource repoSource;
    private final JSONStore jsonStore;
    private RepoOpControl repoOpControl;

    private final List<String> storedRepos;

    private static final int MAX_REDOWNLOAD_TRIES = 2;

    /**
     * Contructs a RepoIO providing IO operations on repositories, taking in various optional
     * parameters for repos source and storage which are useful for testing purposes.
     *
     * @param repoSource     optional source of repos. Default to GitHubSource if not present
     * @param jsonStore      optional storage for repos. Default to a new JSONStore if not present
     * @param storeDirectory optional directory for storing repos. Default value is in RepoStore.
     */
    public RepoIO(Optional<RepoSource> repoSource, Optional<JSONStore> jsonStore,
                  Optional<String> storeDirectory) {
        this.repoSource = repoSource.orElseGet(() -> new GitHubSource());
        storeDirectory.ifPresent((dir) -> RepoStore.changeDirectory(dir));
        this.jsonStore = jsonStore.orElseGet(() -> new JSONStore());
        storedRepos = new ArrayList<>(this.jsonStore.getStoredRepos());
    }

    /**
     * Sets the RepoOpControl instance that this RepoIO can use to execute repo level mutually exclusive operations
     *
     * @param repoOpControl
     */
    public void setRepoOpControl(RepoOpControl repoOpControl) {
        this.repoOpControl = repoOpControl;
    }

    /**
     * Gets the RepoOpControl instance that this RepoIO uses to execute repo level mutually exclusive operations
     */
    public RepoOpControl getRepoOpControl() {
        return this.repoOpControl;
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
                                                                                repoName.equalsIgnoreCase(repoId))
                .findFirst();
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

    public CompletableFuture<Boolean> removeRepository(String repoId) {
        storedRepos.remove(repoId);
        return jsonStore.removeStoredRepo(repoId);
    }

    private CompletableFuture<Model> loadRepoFromStoreAsync(String repoId) {
        return jsonStore.loadRepository(repoId)
                .thenCompose((model) -> this.updateModel(model, false));
    }

    private CompletableFuture<Model> downloadRepoFromSourceAsync(String repoID) {
        return downloadRepoFromSourceAsync(repoID, MAX_REDOWNLOAD_TRIES);
    }

    private CompletableFuture<Model> downloadRepoFromSourceAsync(String repoId, int remainingTries) {
        UI.status.displayMessage("Downloading " + repoId);
        return repoSource.downloadRepository(repoId)
                .thenCompose(newModel -> updateModel(newModel, false, remainingTries))
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

    /**
     * Downloads updates for issues, pull requests, labels, milestones and users from server for a model.
     * Note that the result contains only new or modified data for the model and doesn't include existing data.
     *
     * @param model Model whose updates are to be downloaded
     */
    public CompletableFuture<GitHubModelUpdatesData> downloadModelUpdates(Model model) {
        return repoSource.downloadModelUpdates(model);
    }

    /**
     * Updates a repository, represented locally by a Model, by downloading, reconciling the updates
     * with the existing Model and replacing that Model in {@link Logic#models}.
     *
     * @param model         the Model to be updated
     * @param syncOperation true if this update operation is to be mutually exclusive with other operation
     *                      on the same repository. This is to prevent deadlock when this method is called
     *                      inside another method that is already synced
     * @return
     */
    public CompletableFuture<Model> updateModel(Model model, boolean syncOperation) {
        return updateModel(model, syncOperation, MAX_REDOWNLOAD_TRIES);
    }

    public CompletableFuture<Model> updateModel(Model model, boolean syncOperation, int remainingTries) {
        return downloadModelUpdates(model)
                .thenCompose((updates) -> getRepoOpControl().updateLocalModel(updates, syncOperation))
                .thenApply(newModel -> {
                    boolean corruptedJson = false;
                    if (!model.equals(newModel)) {
                        try {
                            corruptedJson =
                                    jsonStore.saveRepository(newModel.getRepoId(), new SerializableModel(newModel))
                                            .get();
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
                                            "We were not able to sync with GitHub "
                                            + "to retrieve and store data for the repository "
                                            + model.getRepoId()
                                            + ". Please let us know if you "
                                            + "encounter this issue consistently."));
                        } else {
                            UI.status.displayMessage(model.getRepoId() + " is up to date!");
                        }
                        UI.events.triggerEvent(new UpdateProgressEvent(model.getRepoId()));
                        return newModel;
                    }
                }).exceptionally(withResult(new Model(model.getRepoId())));
    }

    public CompletableFuture<Map<Integer, IssueMetadata>> getIssueMetadata(String repoId, List<TurboIssue> issues) {
        return repoSource.downloadMetadata(repoId, issues);
    }

    public CompletableFuture<Boolean> replaceIssueLabels(TurboIssue issue, List<String> labels) {
        return repoSource.replaceIssueLabels(issue, labels);
    }

    public CompletableFuture<Boolean> replaceIssueMilestone(TurboIssue issue, Optional<Integer> milestone) {
        return repoSource.replaceIssueMilestone(issue, milestone);
    }

    public CompletableFuture<Boolean> editIssueState(TurboIssue issue, boolean isOpen) {
        return repoSource.editIssueState(issue, isOpen);
    }

    public CompletableFuture<ImmutablePair<Integer, Long>> getRateLimitResetTime() {
        return repoSource.getRateLimitResetTime();
    }

}
