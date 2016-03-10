package backend;

import backend.control.RepoOpControl;
import backend.resource.Model;
import backend.resource.MultiModel;
import backend.resource.TurboIssue;
import filter.expression.FilterExpression;
import filter.expression.Qualifier;
import filter.expression.QualifierType;
import javafx.application.Platform;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.Logger;
import prefs.Preferences;
import ui.GuiElement;
import ui.TestController;
import ui.UI;
import ui.issuepanel.FilterPanel;
import util.Futures;
import util.HTLog;
import util.Utility;
import util.events.*;
import util.events.testevents.ClearLogicModelEvent;
import util.events.testevents.ClearLogicModelEventHandler;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static util.Futures.withResult;

public class Logic {

    private static final Logger logger = HTLog.get(Logic.class);

    private final MultiModel models;
    private final UIManager uiManager;
    protected final Preferences prefs;
    private final RepoIO repoIO = TestController.createApplicationRepoIO();

    private final RepoOpControl repoOpControl;
    public LoginController loginController;
    public UpdateController updateController;

    public Logic(UIManager uiManager, Preferences prefs, Optional<MultiModel> models) {
        this.uiManager = uiManager;
        this.prefs = prefs;
        this.models = models.orElse(new MultiModel(prefs));

        repoOpControl = RepoOpControl.createRepoOpControl(repoIO, this.models);
        loginController = new LoginController(this);
        updateController = new UpdateController(this);

        // Only relevant to testing, need a different event type to avoid race condition
        UI.events.registerEvent((ClearLogicModelEventHandler) this::onLogicModelClear);
    }

    private void onLogicModelClear(ClearLogicModelEvent e) {
        // DELETE_* and RESET_REPO is handled jointly by Logic and DummyRepo
        assert TestController.isTestMode();
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
    }

    private CompletableFuture<Boolean> isRepositoryValid(String repoId) {
        return repoIO.isRepositoryValid(repoId);
    }

    public void refresh() {
        String message = "Refreshing " + models.toModels().stream()
                .map(Model::getRepoId)
                .collect(Collectors.joining(", "));

        logger.info(message);
        UI.status.displayMessage(message);

        Futures.sequence(models.toModels().stream()
                .map((model) -> repoIO.updateModel(model, true))
                .collect(Collectors.toList()))
                .thenRun(this::refreshUI)
                .thenCompose(n -> getRateLimitResetTime())
                .thenApply(this::updateRemainingRate)
                .exceptionally(Futures::log);
    }

    /**
     * Opens repoId if it isn't already open, else simply refreshes the UI
     * After opening the repo, it will trigger a PrimaryRepoOpenedEvent
     *
     * @param repoId
     * @return
     */
    public CompletableFuture<Boolean> openPrimaryRepository(String repoId) {
        return openRepository(repoId, Optional.empty());
    }

    /**
     * Opens repoId if it isn't already open, else simply refreshes the UI
     *
     * If a repo needs to be opened, FilterRepoOpeningEvent and FilterRepoOpenedEvent will both
     * be triggered with panel as argument
     *
     * Shortly before this method terminates, an AppliedFilterEvent will be triggered
     *
     * @param repoId
     * @param panel panel that opened the repository
     * @return
     */
    public CompletableFuture<Boolean> openRepositoryFromFilter(String repoId, FilterPanel panel) {
        return openRepository(repoId, Optional.of(panel));
    }

    /**
     * Opens repoId if it isn't already open, else simply refreshes the UI
     *
     * During the process, it will trigger the appropriate events depending on panel's presence
     *
     * @param repoId id of repository to be opened
     * @param panel panel that opened the repository, if there is
     * @return
     */
    private CompletableFuture<Boolean> openRepository(String repoId, Optional<FilterPanel> panel) {
        assert Utility.isWellFormedRepoId(repoId);

        boolean isPrimaryRepository = !panel.isPresent();
        if (isPrimaryRepository) prefs.setLastViewedRepository(repoId);
        if (isAlreadyOpen(repoId) || models.isRepositoryPending(repoId)) {
            if (isPrimaryRepository) {
                // The content of panels with an empty filter text should change when the primary repo is changed.
                // Thus we refresh panels even when the repo is already open.
                refreshUI();
            } else {
                Platform.runLater(() -> UI.events.triggerEvent(new AppliedFilterEvent(panel.get())));
            }
            return Futures.unit(false);
        }
        models.queuePendingRepository(repoId);
        return isRepositoryValid(repoId).thenCompose(valid -> {
            if (!valid) {
                return Futures.unit(false);
            }

            logger.info("Opening " + repoId);
            UI.status.displayMessage("Opening " + repoId);
            notifyRepoOpening(isPrimaryRepository);

            return repoOpControl.openRepository(repoId)
                    .thenApply(models::addPending)
                    .thenRun(this::refreshUI)
                    .thenRun(() -> notifyRepoOpened(panel))
                    .thenCompose(n -> getRateLimitResetTime())
                    .thenApply(this::updateRemainingRate)
                    .thenApply(rateLimits -> true)
                    .exceptionally(withResult(false));
        });
    }

    /**
     * Triggers opening repo event based on isPrimaryRepository
     * @param isPrimaryRepository triggers PrimaryRepoOpeningEvent if true, FilterRepoOpeningEvent otherwise
     */
    private void notifyRepoOpening(boolean isPrimaryRepository) {
        Event eventToTrigger = isPrimaryRepository ? new PrimaryRepoOpeningEvent() : new FilterRepoOpeningEvent();
        Platform.runLater(() -> UI.events.triggerEvent(eventToTrigger));
    }

    /**
     * Triggers the relevant event(s) based on panel's presence
     *
     * If panel is present, it will trigger FilterRepoOpenedEvent and AppliedFilterEvent
     * Otherwise, it will simply trigger PrimaryRepoOpenedEvent
     *
     * @param panel panel that opened the repository, if present
     */
    private void notifyRepoOpened(Optional<FilterPanel> panel) {
        if (!panel.isPresent()) {
            Platform.runLater(() -> UI.events.triggerEvent(new PrimaryRepoOpenedEvent()));
            return;
        }

        Platform.runLater(() -> UI.events.triggerEvent(new FilterRepoOpenedEvent()));
        Platform.runLater(() -> UI.events.triggerEvent(new AppliedFilterEvent(panel.get())));
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
        return repoOpControl.removeRepository(repoId);
    }

    /**
     * Recommended Pre-condition: normalize reposInUse to lower case
     *                           - using Utility.convertSetToLowerCase()
     */
    public void removeUnusedModels(Set<String> reposInUse) {
        models.toModels().stream().map(Model::getRepoId)
                .filter(repoId -> !reposInUse.contains(repoId.toLowerCase()))
                .forEach(models::removeRepoModelById);
    }

    public ImmutablePair<Integer, Long> updateRemainingRate
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
     * Replaces existing labels with new labels in the issue object, the UI, and the server, in that order.
     * Server update is done after the local update to reduce the lag between the user action and the UI response
     *
     * @param issue The issue object whose labels are to be replaced.
     * @param newLabels The list of new labels to be assigned to the issue.
     * @return true if label replacement on GitHub was a success, false otherwise.
     */
    public CompletableFuture<Boolean> replaceIssueLabels(TurboIssue issue, List<String> newLabels) {
        List<String> originalLabels = issue.getLabels();

        logger.info("Changing labels for " + issue + " on UI");
        CompletableFuture<Optional<TurboIssue>> localReplaceResult =
                repoOpControl.replaceIssueLabelsLocally(issue, newLabels);
        if (localReplaceResult.isCompletedExceptionally() || localReplaceResult.isCancelled()) {
            return CompletableFuture.completedFuture(false);
        }
        Optional<TurboIssue> locallyModifiedIssue = localReplaceResult.join();
        if (!locallyModifiedIssue.isPresent()) {
            return CompletableFuture.completedFuture(false);
        }
        localReplaceResult.thenRun(this::refreshUI);

        return updateIssueLabelsOnServer(issue, newLabels)
                .thenApply((isUpdateSuccessful) -> handleIssueLabelsUpdateOnServerResult(
                            isUpdateSuccessful, locallyModifiedIssue.get(), originalLabels));
    }

    /**
     * Gets the issue identified by {@code repoId} and {@code issueId} in {@link Logic#models}
     * @param repoId
     * @param issueId
     * @return
     */
    private Optional<TurboIssue> getIssue(String repoId, int issueId) {
        Optional<Model> modelLookUpResult = models.getModelById(repoId);
        return Utility.safeFlatMapOptional(modelLookUpResult,
                (model) -> model.getIssueById(issueId),
                () -> logger.error("Model " + repoId + " not found in models"));
    }

    private CompletableFuture<Boolean> updateIssueLabelsOnServer(TurboIssue issue, List<String> newLabels) {
        logger.info("Changing labels for " + issue + " on GitHub");
        return repoOpControl.replaceIssueLabelsOnServer(issue, newLabels);
    }

    /**
     * Handles the result of updating an issue's labels on server. Current implementation includes
     * reverting back to the original labels locally if the server update failed.
     * @param isUpdateSuccessful
     * @param localModifiedIssue
     * @param originalLabels
     * @return true if the server update is successful
     */
    private boolean handleIssueLabelsUpdateOnServerResult(boolean isUpdateSuccessful,
                                                          TurboIssue localModifiedIssue,
                                                          List<String> originalLabels) {
        if (isUpdateSuccessful) {
            return true;
        }
        logger.error("Unable to update model on server");
        revertLocalLabelsReplace(localModifiedIssue, originalLabels);
        return false;
    }

    /**
     * Replaces labels of the issue in the {@link Logic#models} corresponding to {@code modifiedIssue} with
     * {@code originalLabels} if the current labels on the issue is assigned at the same time as {@code modifiedIssue}
     * @param modifiedIssue
     * @param originalLabels
     */
    private void revertLocalLabelsReplace(TurboIssue modifiedIssue, List<String> originalLabels) {
        TurboIssue currentIssue = getIssue(modifiedIssue.getRepoId(), modifiedIssue.getId()).orElse(modifiedIssue);
        LocalDateTime originalLabelsModifiedAt = modifiedIssue.getLabelsLastModifiedAt();
        LocalDateTime currentLabelsAssignedAt = currentIssue.getLabelsLastModifiedAt();
        boolean isCurrentLabelsModifiedFromOriginalLabels = originalLabelsModifiedAt.isEqual(currentLabelsAssignedAt);

        if (isCurrentLabelsModifiedFromOriginalLabels) {
            logger.info("Reverting labels for issue " + currentIssue);
            models.replaceIssueLabels(currentIssue.getRepoId(), currentIssue.getId(), originalLabels);
            refreshUI();
        }
    }

    /**
     * Determines data to be sent to the GUI to refresh the entire GUI with the current model in Logic,
     * and then sends the data to the GUI.
     */
    private void refreshUI() {
        updateController.processAndRefresh(getAllPanels());
    }

    /**
     * Feeds the panel's filter expression to updateController.
     *
     * @param panel The panel whose filter expression is to be processed by updateController.
     */
    public void refreshPanel(FilterPanel panel) {
        List<FilterPanel> panels = new ArrayList<>();
        panels.add(panel);
        updateController.processAndRefresh(panels);

        // AppliedFilterEvent will be triggered asynchronously when repo(s) have finished opening, so just terminate
        if (hasRepoSpecifiedInFilter(panel)) return;

        Platform.runLater (() -> UI.events.triggerEvent(new AppliedFilterEvent(panel)));
    }

    private boolean hasRepoSpecifiedInFilter(FilterPanel panel) {
        return !Qualifier.getMetaQualifierContent(panel.getCurrentFilterExpression(), QualifierType.REPO).isEmpty();
    }

    /**
     * Retrieves metadata for given issues from the repository source, and then processes them for non-self
     * update timings.
     *
     * @param repoId The repository containing issues to retrieve metadata for.
     * @param issues Issues sharing the same repository requiring a metadata update.
     * @return True if metadata retrieval was a success, false otherwise.
     */
    public CompletableFuture<Boolean> getIssueMetadata(String repoId, List<TurboIssue> issues) {
        String message = "Getting metadata for " + repoId + "...";
        logger.info("Getting metadata for issues " + issues);
        UI.status.displayMessage(message);

        return repoIO.getIssueMetadata(repoId, issues).thenApply(this::processUpdates)
                .thenApply(metadata -> insertMetadata(metadata, repoId, prefs.getLastLoginUsername()))
                .exceptionally(withResult(false));
    }

    private boolean insertMetadata(Map<Integer, IssueMetadata> metadata, String repoId, String currentUser) {
        String updatedMessage = "Received metadata from " + repoId + "!";
        UI.status.displayMessage(updatedMessage);
        models.insertMetadata(repoId, metadata, currentUser);
        return true;
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

    /**
     * Carries the current set of GUI elements, as well as the current list of users in the model, to the GUI.
     */
    public void updateUI(Map<FilterExpression, List<GuiElement>> elementsToShow) {
        uiManager.update(elementsToShow, models.getUsers());
    }

    private List<FilterPanel> getAllPanels() {
        return uiManager.getAllPanels();
    }

    /**
     * For use by UpdateController to perform filtering.
     *
     * @return The currently held MultiModel.
     */
    public MultiModel getModels() {
        return models;
    }
}
