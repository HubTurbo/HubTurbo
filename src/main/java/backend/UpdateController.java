package backend;

import backend.resource.Model;
import backend.resource.MultiModel;
import backend.resource.TurboIssue;
import filter.expression.FilterExpression;
import filter.expression.Qualifier;
import org.apache.logging.log4j.Logger;
import filter.expression.QualifierType;
import ui.GuiElement;
import ui.issuepanel.FilterPanel;
import util.Futures;
import util.HTLog;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Manages the flow of logic during a data retrieval cycle from the repository source.
 *
 * The central logic is contained in processAndRefresh, while the remaining methods are auxiliary methods to be
 * called by processAndRefresh.
 */
public class UpdateController {
    private static final Logger logger = HTLog.get(UpdateController.class);

    private final Logic logic;

    public UpdateController(Logic logic) {
        this.logic = logic;
    }

    /**
     * Given a list of panels, opens the repositories specified in the panels' filters.
     *
     * After which, dispatches metadata update if needed and then processes them to return
     * a map of filtered and sorted issues corresponding to each filter expression, based on the most recent data
     * from the repository source.
     *
     * @param filterPanels Filter panels to process
     */
    public void processAndRefresh(List<FilterPanel> filterPanels) {
        List<FilterExpression> filterExprs = getFilterExpressions(filterPanels);

        // Filter and sort the issues first even if the metadata is not yet available so that criteria not
        // based on metadata can have immediate effect.
        logic.updateUI(processFilter(filterExprs));

        // Open specified repos
        openRepositoriesInFilters(filterPanels)
        .thenRun(() -> {
            // First filter, for issues requiring a metadata update.
            Map<String, List<TurboIssue>> toUpdate = tallyMetadataUpdate(filterExprs);

            if (toUpdate.isEmpty()) {
                // If no issues requiring metadata update, just run the filter and sort.
                logic.updateUI(processFilter(filterExprs));
                return;
            }

            // If there are issues requiring metadata update, we dispatch the metadata requests...
            ArrayList<CompletableFuture<Boolean>> metadataRetrievalTasks = new ArrayList<>();
            toUpdate.forEach((repoId, issues) ->
                    metadataRetrievalTasks.add(logic.getIssueMetadata(repoId, issues)));
            // ...and then wait for all of them to complete.
            Futures.sequence(metadataRetrievalTasks)
                    .thenAccept(results -> logger.info("Metadata retrieval successful for "
                            + results.stream().filter(result -> result).count() + "/"
                            + results.size() + " repos"))
                    .thenCompose(n -> logic.getRateLimitResetTime())
                    .thenApply(logic::updateRemainingRate)
                    .thenRun(() -> logic.updateUI(processFilter(filterExprs))); // Then filter the second time.
        });
    }

    private List<FilterExpression> getFilterExpressions(List<FilterPanel> panels) {
        return panels.stream()
                .map(panel -> panel.getCurrentFilterExpression())
                .collect(Collectors.toList());
    }

    /**
     * Given a list of filter panels, opens all repositories necessary for processing the filter expressions
     *
     * @param filterPanels
     */
    private CompletableFuture<List<Boolean>> openRepositoriesInFilters(List<FilterPanel> filterPanels) {
        return Futures.sequence(filterPanels.stream()
                .flatMap(panel -> Qualifier.getMetaQualifierContent(panel.getCurrentFilterExpression(),
                        QualifierType.REPO).stream()
                        .map(repoId -> logic.openRepositoryFromFilter(repoId, panel)))
                .collect(Collectors.toList()));
    }

    /**
     * Given a list of filter expressions, determine issues within the model that require a metadata update.
     *
     * @param filterExprs Filter expressions to process for metadata requests.
     * @return Repo IDs and the corresponding issues in the repo requiring a metadata update.
     */
    private Map<String, List<TurboIssue>> tallyMetadataUpdate(List<FilterExpression> filterExprs) {
        MultiModel models = logic.getModels();
        List<TurboIssue> allModelIssues = models.getIssues();

        return filterExprs.stream()
                .filter(Qualifier::hasUpdatedQualifier)
                .flatMap(filterExpr -> allModelIssues.stream()
                        .filter(issue -> Qualifier.process(models, filterExpr, issue)))
                .distinct()
                .collect(Collectors.groupingBy(TurboIssue::getRepoId));
    }

    /**
     * Filters, sorts and counts issues within the model according to the given filter expressions.
     * In here, "processed" is equivalent to "filtered, sorted and counted".
     *
     * @param filterExprs Filter expressions to process.
     * @return Filter expressions and their corresponding issues after filtering, sorting and counting.
     */
    private Map<FilterExpression, List<GuiElement>> processFilter(List<FilterExpression> filterExprs) {
        MultiModel models = logic.getModels();
        List<TurboIssue> allModelIssues = models.getIssues();

        Map<FilterExpression, List<GuiElement>> processed = new HashMap<>();

        filterExprs.stream().distinct().forEach(filterExpr -> {
            boolean hasUpdatedQualifier = Qualifier.hasUpdatedQualifier(filterExpr);

            FilterExpression filterExprNoAlias = Qualifier.replaceMilestoneAliases(models, filterExpr);

            List<TurboIssue> processedIssues = allModelIssues.stream()
                    .filter(issue -> Qualifier.process(models, filterExprNoAlias, issue))
                    .sorted(determineComparator(filterExprNoAlias, hasUpdatedQualifier))
                    .limit(Qualifier.determineCount(allModelIssues, filterExprNoAlias))
                    .collect(Collectors.toList());

            List<GuiElement> processedElements = produceGuiElements(models, processedIssues);

            processed.put(filterExpr, processedElements);
        });

        return processed;
    }

    /**
     * Produces a suitable comparator based on the given filter expression.
     *
     * @param filterExpr          The given filter expression.
     * @param hasUpdatedQualifier Determines the behaviour of the sort key "nonSelfUpdate".
     * @return The comparator to use.
     */
    private Comparator<TurboIssue> determineComparator(FilterExpression filterExpr,
                                                       boolean hasUpdatedQualifier) {
        MultiModel models = logic.getModels();
        for (Qualifier metaQualifier : filterExpr.find(Qualifier::isMetaQualifier)) {
            // Only take into account the first sort qualifier found
            if (metaQualifier.getType() == QualifierType.SORT) {
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
     * Constructs GuiElements (including all necessary references to labels/milestones/users to properly display
     * the issue) corresponding to a list of issues without changing the order.
     *
     * @param models The MultiModel from which necessary references are extracted.
     * @param processedIssues The list of issues to construct GUIElements for.
     * @return A list of GUIElements corresponding to the given list of issues.
     */
    private List<GuiElement> produceGuiElements(MultiModel models, List<TurboIssue> processedIssues) {
        return processedIssues.stream().map(issue -> {
            Optional<Model> modelOfIssue = models.getModelById(issue.getRepoId());
            assert modelOfIssue.isPresent();

            return new GuiElement(issue,
                    models.getLabelsOfIssue(issue),
                    models.getMilestoneOfIssue(issue),
                    models.getAssigneeOfIssue(issue),
                    models.getAuthorOfIssue(issue)
            );
        }).collect(Collectors.toList());
    }
}
