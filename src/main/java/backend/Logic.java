package backend;

import backend.resource.Model;
import backend.resource.MultiModel;
import backend.resource.TurboIssue;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.Logger;
import prefs.Preferences;
import ui.UI;
import util.Futures;
import util.HTLog;
import util.Utility;
import util.events.RepoOpenedEvent;
import util.events.testevents.ClearLogicModelEventHandler;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static util.Futures.withResult;

public class Logic {

    private static final Logger logger = HTLog.get(Logic.class);

    private final MultiModel models;
    private final UIManager uiManager;
    protected final Preferences prefs;

    private RepoIO repoIO;
    public LoginController loginController;

    public Logic(UIManager uiManager, Preferences prefs, boolean isTestMode, boolean enableTestJSON) {
        this.uiManager = uiManager;
        this.prefs = prefs;
        this.models = new MultiModel(prefs);

        repoIO = new RepoIO(isTestMode, enableTestJSON);
        loginController = new LoginController(this);

        // Only relevant to testing, need a different event type to avoid race condition
        UI.events.registerEvent((ClearLogicModelEventHandler) e -> {
            // DELETE_* and RESET_REPO is handled jointly by Logic and DummyRepo
            assert isTestMode;
            assert e.repoId != null;

            List<Model> toReplace = models.toModels();

            logger.info("Attempting to reset " + e.repoId);
            if (toReplace.remove(models.get(e.repoId))) {
                logger.info("Clearing " + e.repoId + " successful.");
            } else {
                logger.info(e.repoId + " not currently in model.");
            }
            models.replace(toReplace);

            // Re-"download" repo after clearing
            openPrimaryRepository(e.repoId);
        });

        // Pass the currently-empty model to the UI
        uiManager.updateNow(models);
    }

    private CompletableFuture<Boolean> isRepositoryValid(String repoId) {
        return repoIO.isRepositoryValid(repoId);
    }

    public void refresh(boolean isNotificationPaneShowing) {
        // TODO fix refresh to take into account the possible pending actions associated with the notification pane
        if (isNotificationPaneShowing) {
            logger.info("Notification Pane is currently showing, not going to refresh. ");
            return;
        }
        String message = "Refreshing " + models.toModels().stream()
                .map(Model::getRepoId)
                .collect(Collectors.joining(", "));

        logger.info(message);
        UI.status.displayMessage(message);

        Futures.sequence(models.toModels().stream()
                .map(repoIO::updateModel)
                .collect(Collectors.toList()))
                .thenApply(models::replace)
                .thenRun(this::updateUI)
                .thenCompose(n -> getRateLimitResetTime())
                .thenApply(this::updateRemainingRate)
                .exceptionally(Futures::log);
    }

    public CompletableFuture<Boolean> openPrimaryRepository(String repoId) {
        return openRepository(repoId, true);
    }

    public CompletableFuture<Boolean> openRepositoryFromFilter(String repoId) {
        return openRepository(repoId, false);
    }

    public CompletableFuture<Boolean> openRepository(String repoId, boolean isPrimaryRepository) {
        assert Utility.isWellFormedRepoId(repoId);
        if (isPrimaryRepository) prefs.setLastViewedRepository(repoId);
        if (isAlreadyOpen(repoId) || models.isRepositoryPending(repoId)) {
            // The content of panels with an empty filter text should change when the primary repo is changed.
            // Thus we call updateUI even when the repo is already open.
            if (isPrimaryRepository) updateUI();
            return Futures.unit(false);
        }
        models.queuePendingRepository(repoId);
        return isRepositoryValid(repoId).thenCompose(valid -> {
            if (!valid) {
                return Futures.unit(false);
            } else {
                logger.info("Opening " + repoId);
                UI.status.displayMessage("Opening " + repoId);
                return repoIO.openRepository(repoId)
                        .thenApply(models::addPending)
                        .thenRun(this::updateUI)
                        .thenRun(() -> UI.events.triggerEvent(new RepoOpenedEvent(repoId)))
                        .thenCompose(n -> getRateLimitResetTime())
                        .thenApply(this::updateRemainingRate)
                        .thenApply(rateLimits -> true)
                        .exceptionally(withResult(false));
            }
        });
    }

    public CompletableFuture<Boolean> getIssueMetadata(String repoId, List<TurboIssue> issues) {
        String message = "Getting metadata for " + repoId + "...";
        logger.info("Getting metadata for issues "
                + issues.stream().map(TurboIssue::getId).map(Object::toString).collect(Collectors.joining(", ")));
        UI.status.displayMessage(message);

        String currentUser = prefs.getLastLoginUsername();

        return repoIO.getIssueMetadata(repoId, issues).thenApply(this::processUpdates)
            .thenApply(metadata -> {
                String updatedMessage = "Received metadata from " + repoId + "!";
                UI.status.displayMessage(updatedMessage);
                models.insertMetadata(repoId, metadata, currentUser);
                return metadata;
            })
            .thenApply(Futures.tap(this::updateUIAndShow))
            .thenCompose(n -> getRateLimitResetTime())
            .thenApply(this::updateRemainingRate)
            .thenApply(rateLimits -> true)
            .exceptionally(withResult(false));
    }

    // Adds update times to the metadata map
    private Map<Integer, IssueMetadata> processUpdates(Map<Integer, IssueMetadata> metadata) {
        String currentUser = prefs.getLastLoginUsername();

        // Iterates through each entry in the metadata set, and looks for the comment/event with
        // the latest time created.
        for (Map.Entry<Integer, IssueMetadata> entry : metadata.entrySet()) {
            IssueMetadata currentMetadata = entry.getValue();

            entry.setValue(currentMetadata.full(currentUser));
        }
        return metadata;
    }

    public Set<String> getOpenRepositories() {
        return models.toModels().stream().map(Model::getRepoId).map(String::toLowerCase).collect(Collectors.toSet());
    }

    public Set<String> getStoredRepos() {
        return repoIO.getStoredRepos().stream().collect(Collectors.toSet());
    }

    public boolean isAlreadyOpen(String repoId) {
        return getOpenRepositories().contains(repoId.toLowerCase());
    }

    public void setDefaultRepo(String repoId) {
        models.setDefaultRepo(repoId);
    }

    public String getDefaultRepo() {
        return models.getDefaultRepo();
    }

    public CompletableFuture<Boolean> removeStoredRepository(String repoId) {
        return repoIO.removeRepository(repoId);
    }

    /**
     * Recommended Pre-condition: normalize reposInUse to lower case
     *                           - using Utility.convertSetToLowerCase()
     */
    public void removeUnusedModels(Set<String> reposInUse) {
        models.toModels().stream().map(Model::getRepoId)
                .filter(repoId -> !reposInUse.contains(repoId.toLowerCase()))
                .forEach(repoIdNotInUse -> models.removeRepoModelById(repoIdNotInUse));
    }

    /**
     * Carries the current model in Logic to the GUI and triggers metadata updates if panels require
     * metadata to display their issues, in which case the changes in the model are not presented to the user.
     */
    private void updateUI() {
        uiManager.update(models, false);
    }

    /**
     * Carries the current model in Logic to the GUI and immediately presents it to the user. Does not trigger
     * further metadata updates.
     */
    private void updateUIAndShow() {
        uiManager.update(models, true);
    }

    private ImmutablePair<Integer, Long> updateRemainingRate
            (ImmutablePair<Integer, Long> rateLimits) {
        uiManager.updateRateLimits(rateLimits);
        return rateLimits;
    }

    protected CompletableFuture<Boolean> repoIOLogin(UserCredentials credentials) {
        return repoIO.login(credentials);
    }

    public Model getRepo(String repoId) {
        return models.get(repoId);
    }

    public CompletableFuture<ImmutablePair<Integer, Long>> getRateLimitResetTime() {
        return repoIO.getRateLimitResetTime();
    }

    /**
     * Dispatches a PUT request to the GitHub API to replace the given issue's labels.
     * At the same time, immediately change the GUI to pre-empt this change.
     *
     * Assumes that the model object is shared among GUI and Logic.
     *
     * @param issue The issue whose labels are to be replaced
     * @param labels The labels to be applied to the given issue
     * @param originalLabels The original labels to be applied to the UI in case of failure
     * @return A boolean indicating the result of the label replacement from GitHub
     */
    public CompletableFuture<Boolean> replaceIssueLabelsRepo
            (TurboIssue issue, List<String> labels, List<String> originalLabels) {
        logger.info(HTLog.format(issue.getRepoId(), "Sending labels " + labels + " for " + issue + " to GitHub"));
        return repoIO.replaceIssueLabels(issue, labels)
                .thenApply(e -> true)
                .exceptionally(e -> {
                    replaceIssueLabelsUI(issue, originalLabels);
                    logger.error(e.getLocalizedMessage(), e);
                    return false;
        });
    }

    public void replaceIssueLabelsUI(TurboIssue issue, List<String> labels) {
        logger.info(HTLog.format(issue.getRepoId(), "Applying labels " + labels + " to " + issue + " in UI"));
        issue.setLabels(labels);
        updateUIAndShow();
    }

}
