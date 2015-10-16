package backend;

import backend.resource.Model;
import backend.resource.MultiModel;
import backend.resource.TurboIssue;
import filter.expression.FilterExpression;
import filter.expression.Qualifier;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.Logger;
import prefs.Preferences;
import ui.UI;
import util.Futures;
import util.HTLog;
import util.Utility;
import util.events.RepoOpenedEvent;
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
        uiManager.updateEmpty(models);
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

    public CompletableFuture<Boolean> openRepository(String repoId, boolean isPrimaryRepository) {
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
                return repoIO.openRepository(repoId)
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

    public void removeUnusedModels(Set<String> reposInUse) {
        models.toModels().stream().map(Model::getRepoId).
                filter(repoId -> !reposInUse.contains(repoId)).
                forEach(repoIdNotInUse -> models.removeRepoModelById(repoIdNotInUse));
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
        refreshUI();
    }

    /**
     * Determines data to be sent to the GUI to refresh the entire GUI with the current model in Logic,
     * and then sends the data to the GUI.
     */
    private void refreshUI() {
        filterSortRefresh(getAllUIFilters());
    }

    /**
     * Given a list of filter expressions, dispatch metadata update if needed and then process them to return
     * a map of filtered and sorted issues corresponding to each filter expression, based on the most recent data
     * from the repository source.
     *
     * @param filterExprs Filter expressions to process
     */
    public void filterSortRefresh(List<FilterExpression> filterExprs) {
        // Open specified repos
        openRepositoriesInFilters(filterExprs);

        // First filter, for issues requiring a metadata update.
        Map<String, List<TurboIssue>> toUpdate = tallyMetadataUpdate(filterExprs);

        if (toUpdate.size() > 0) {
            // If there are issues requiring metadata update, we dispatch the metadata requests...
            ArrayList<CompletableFuture<Boolean>> metadataRetrievalTasks = new ArrayList<>();
            toUpdate.forEach((repoId, issues) -> {
                metadataRetrievalTasks.add(getIssueMetadata(repoId, issues));
            });
            // ...and then wait for all of them to complete.
            Futures.sequence(metadataRetrievalTasks)
                    .thenAccept(results -> logger.info("Metadata retrieval successful for "
                            + results.stream().filter(result -> result).count() + "/"
                            + results.size() + " repos"))
                    .thenCompose(n -> getRateLimitResetTime())
                    .thenApply(this::updateRemainingRate)
                    .thenRun(() -> updateUI(filterAndSort(filterExprs))); // Then filter the second time.
        } else {
            // If no issues requiring metadata update, just run the filter and sort.
            updateUI(filterAndSort(filterExprs));
        }
    }

    /**
     * Given a list of filter expressions, open all repositories necessary for processing the filter expressions.
     *
     * @param filterExprs Filter expressions to process.
     */
    private void openRepositoriesInFilters(List<FilterExpression> filterExprs) {
        filterExprs.stream()
                .flatMap(filterExpr -> Qualifier.getContentOfMetaQualifier(filterExpr, Qualifier.REPO).stream())
                .distinct()
                .forEach(this::openRepositoryFromFilter);
    }

    /**
     * Given a list of filter expressions, determine issues within the model that require a metadata update.
     *
     * @param filterExprs Filter expressions to process for metadata requests.
     * @return Repo IDs and the corresponding issues in the repo requiring a metadata update.
     */
    private Map<String, List<TurboIssue>> tallyMetadataUpdate(List<FilterExpression> filterExprs) {
        List<TurboIssue> allModelIssues = models.getIssues();

        return filterExprs.stream()
                .filter(Qualifier::hasUpdatedQualifier)
                .flatMap(filterExpr -> allModelIssues.stream()
                        .filter(issue -> Qualifier.process(models, filterExpr, issue)))
                .distinct()
                .collect(Collectors.groupingBy(TurboIssue::getRepoId));
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
        logger.info("Getting metadata for issues "
                + issues.stream().map(TurboIssue::getId).map(Object::toString).collect(Collectors.joining(", ")));
        UI.status.displayMessage(message);

        String currentUser = prefs.getLastLoginUsername();

        return repoIO.getIssueMetadata(repoId, issues).thenApply(this::processUpdates)
                .thenApply(metadata -> {
                    String updatedMessage = "Received metadata from " + repoId + "!";
                    UI.status.displayMessage(updatedMessage);
                    models.insertMetadata(repoId, metadata, currentUser);
                    return true;
                })
                .exceptionally(withResult(false));
    }

    /**
     * Filters and sorts issues within the model according to the given filter expressions.
     *
     * @param filterExprs Filter expressions to process.
     * @return Filter expressions and their corresponding issues after filtering and sorting.
     */
    private Map<FilterExpression, ImmutablePair<List<TurboIssue>, Boolean>>
                                        filterAndSort(List<FilterExpression> filterExprs) {
        Map<FilterExpression, ImmutablePair<List<TurboIssue>, Boolean>> filteredAndSorted = new HashMap<>();

        List<TurboIssue> allModelIssues = models.getIssues();

        filterExprs.forEach(filterExpr -> {
            List<TurboIssue> filterAndSortedExpression = filteredAndSorted.get(filterExpr);

            if (filterAndSortedExpression == null) { // If it already exists, no need to filter anymore
                boolean hasUpdatedQualifier = Qualifier.hasUpdatedQualifier(filterExpr);

                List<TurboIssue> filteredAndSortedIssues = allModelIssues.stream()
                        .filter(issue -> Qualifier.process(models, filterExpr, issue))
                        .sorted(determineComparator(filterExpr, hasUpdatedQualifier))
                        .collect(Collectors.toList());

                filteredAndSorted.put(filterText, new ImmutablePair<>(filteredAndSortedIssues, hasUpdatedQualifier));
            }
        });

        return filteredAndSorted;
    }

    /**
     * Produces a suitable comparator based on the given filter expression.
     *
     * @param filterExpr The given filter expression.
     * @param hasUpdatedQualifier Determines the behaviour of the sort key "nonSelfUpdate".
     * @return The comparator to use.
     */
    private Comparator<TurboIssue> determineComparator(FilterExpression filterExpr,
                                                       boolean hasUpdatedQualifier) {
        for (Qualifier metaQualifier : filterExpr.find(Qualifier::isMetaQualifier)) {
            // Only take into account the first sort qualifier found
            if (metaQualifier.getName().equals("sort")) {
                return metaQualifier.getCompoundSortComparator(models, hasUpdatedQualifier);
            }
        }

        // No sort qualifier, look for updated qualifier
        if (hasUpdatedQualifier) {
            return Qualifier.getSortComparator(models, "nonSelfUpdate", true, true);
        }

        // No sort or updated, return sort by descending ID, which is the default.
        return Qualifier.getSortComparator(models, "id", true, false);
    }

    /**
     * Carries the current model in Logic to the GUI and triggers metadata updates if panels require
     * metadata to display their issues, in which case the changes in the model are not presented to the user.
     */
    private void updateUI(Map<FilterExpression, ImmutablePair<List<TurboIssue>, Boolean>> issuesToShow) {
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
}
