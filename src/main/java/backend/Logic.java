package backend;

import backend.control.RepoOpControl;
import backend.resource.Model;
import backend.resource.MultiModel;
import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import filter.expression.FilterExpression;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.Logger;
import prefs.Preferences;
import ui.TestController;
import ui.UI;
import undo.actions.Action;
import undo.actions.ChangeLabelsAction;
import util.Futures;
import util.HTLog;
import util.Utility;
import util.events.RepoOpenedEvent;
import util.events.testevents.ClearLogicModelEvent;
import util.events.testevents.ClearLogicModelEventHandler;

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
    private final RepoOpControl repoOpControl = new RepoOpControl(repoIO);

    public LoginController loginController;
    public UpdateController updateController;

    public Logic(UIManager uiManager, Preferences prefs) {
        this.uiManager = uiManager;
        this.prefs = prefs;
        this.models = new MultiModel(prefs);

        loginController = new LoginController(this);
        updateController = new UpdateController(this);

        // Only relevant to testing, need a different event type to avoid race condition
        UI.events.registerEvent((ClearLogicModelEventHandler) this::onLogicModelClear);

        // Pass the currently-empty model to the UI
        uiManager.updateEmpty(models);
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
                .map(repoOpControl::updateModel)
                .collect(Collectors.toList()))
                .thenApply(models::replace)
                .thenRun(this::refreshUI)
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

    private CompletableFuture<Boolean> openRepository(String repoId, boolean isPrimaryRepository) {
        assert Utility.isWellFormedRepoId(repoId);
        if (isPrimaryRepository) prefs.setLastViewedRepository(repoId);
        if (isAlreadyOpen(repoId) || models.isRepositoryPending(repoId)) {
            // The content of panels with an empty filter text should change when the primary repo is changed.
            // Thus we refresh panels even when the repo is already open.
            if (isPrimaryRepository) refreshUI();
            return Futures.unit(false);
        }
        models.queuePendingRepository(repoId);
        return isRepositoryValid(repoId).thenCompose(valid -> {
            if (!valid) {
                return Futures.unit(false);
            } else {
                logger.info("Opening " + repoId);
                UI.status.displayMessage("Opening " + repoId);
                return repoOpControl.openRepository(repoId)
                        .thenApply(models::addPending)
                        .thenRun(this::refreshUI)
                        .thenRun(() -> UI.events.triggerEvent(new RepoOpenedEvent(repoId)))
                        .thenCompose(n -> getRateLimitResetTime())
                        .thenApply(this::updateRemainingRate)
                        .thenApply(rateLimits -> true)
                        .exceptionally(withResult(false));
            }
        });
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

    // Applies action on issue in UI
    @SuppressWarnings("unchecked")
    public void actOnIssueUI(TurboIssue issue, Action action) {
        logger.info("Applying " + action.getDescription() + " on " + issue + " in UI");
        action.act(issue);
        refreshUI();
    }

    // Applies action on issue in GitHub, returns false if it fails
    @SuppressWarnings("unchecked")
    public CompletableFuture<Boolean> actOnIssueRepo(TurboIssue issue, Action action) {
        logger.info("Applying " + action.getDescription() + " on " + issue + " in GitHub");
        if (action.getClass().equals(ChangeLabelsAction.class)) {
            ChangeLabelsAction changeAction = (ChangeLabelsAction) action;
            return repoIO.replaceIssueLabels(issue, changeAction.act(changeAction.getOriginalIssue()).getLabels())
                            .thenApply(e -> true)
                            .exceptionally(e -> {
                                logger.error(e.getLocalizedMessage(), e);
                                return false;
                            });
        } else {
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * Determines data to be sent to the GUI to refresh the entire GUI with the current model in Logic,
     * and then sends the data to the GUI.
     */
    private void refreshUI() {
        updateController.filterSortRefresh(getAllUIFilters());
    }

    /**
     * Feeds a one-element list of filter expressions to updateController.
     *
     * @param filterExpr The filter expression to be processed by updateController.
     */
    public void refreshPanel(FilterExpression filterExpr) {
        List<FilterExpression> panelExpr = new ArrayList<>();
        panelExpr.add(filterExpr);
        updateController.filterSortRefresh(panelExpr);
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
     * Carries the current model in Logic, as well as issues to be displayed in panels, to the GUI.
     */
    public void updateUI(Map<FilterExpression, List<TurboIssue>> issuesToShow) {
        uiManager.update(models, issuesToShow);
    }

    /**
     * Retrieves all filter expressions in active panels from the UI.
     *
     * @return Filter expressions in the UI.
     */
    private List<FilterExpression> getAllUIFilters() {
        return uiManager.getAllFilters();
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
