package backend;

import backend.resource.MultiModel;
import backend.resource.TurboIssue;
import filter.expression.FilterExpression;
import filter.expression.Qualifier;
import org.apache.logging.log4j.Logger;

import filter.expression.QualifierType;
import util.Futures;
import util.HTLog;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class UpdateController {
    private static final Logger logger = HTLog.get(UpdateController.class);

    private final Logic logic;

    public UpdateController(Logic logic) {
        this.logic = logic;
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

        if (!toUpdate.isEmpty()) {
            // If there are issues requiring metadata update, we dispatch the metadata requests...
            ArrayList<CompletableFuture<Boolean>> metadataRetrievalTasks = new ArrayList<>();
            toUpdate.forEach((repoId, issues) -> {
                metadataRetrievalTasks.add(logic.getIssueMetadata(repoId, issues));
            });
            // ...and then wait for all of them to complete.
            Futures.sequence(metadataRetrievalTasks)
                    .thenAccept(results -> logger.info("Metadata retrieval successful for "
                            + results.stream().filter(result -> result).count() + "/"
                            + results.size() + " repos"))
                    .thenCompose(n -> logic.getRateLimitResetTime())
                    .thenApply(logic::updateRemainingRate)
                    .thenRun(() -> logic.updateUI(processFilter(filterExprs))); // Then filter the second time.
        } else {
            // If no issues requiring metadata update, just run the filter and sort.
            logic.updateUI(processFilter(filterExprs));
        }
    }

    /**
     * Given a list of filter expressions, open all repositories necessary for processing the filter expressions.
     *
     * @param filterExprs Filter expressions to process.
     */
    private void openRepositoriesInFilters(List<FilterExpression> filterExprs) {
        filterExprs.stream()
                .flatMap(filterExpr -> Qualifier.getMetaQualifierContent(filterExpr, QualifierType.REPO).stream())
                .distinct()
                .forEach(logic::openRepositoryFromFilter);
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
     * Filters and sorts issues within the model according to the given filter expressions.
     *
     * @param filterExprs Filter expressions to process.
     * @return Filter expressions and their corresponding issues after filtering, sorting and counting.
     */
    private Map<FilterExpression, List<TurboIssue>> processFilter(List<FilterExpression> filterExprs) {
        MultiModel models = logic.getModels();
        List<TurboIssue> allModelIssues = models.getIssues();

        Map<FilterExpression, List<TurboIssue>> filteredSortedAndCounted = new HashMap<>();

        filterExprs.forEach(filterExpr -> {
            List<TurboIssue> filterAndSortedExpression = filteredSortedAndCounted.get(filterExpr);

            if (filterAndSortedExpression == null) { // If it already exists, no need to filter anymore
                boolean hasUpdatedQualifier = Qualifier.hasUpdatedQualifier(filterExpr);

                FilterExpression filterExprNoAlias = Qualifier.replaceMilestoneAliases(models, filterExpr);

                List<TurboIssue> filteredSortedAndCountedIssues = allModelIssues.stream()
                        .filter(issue -> Qualifier.process(models, filterExprNoAlias, issue))
                        .sorted(determineComparator(filterExprNoAlias, hasUpdatedQualifier))
                        .limit(determineCount(allModelIssues, filterExprNoAlias))
                        .collect(Collectors.toList());

                filteredSortedAndCounted.put(filterExpr, filteredSortedAndCountedIssues);
            }
        });

        return filteredSortedAndCounted;
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
     * Determines the count value to be taken from the first valid count qualifier.
     *
     * @param issueList Issue list obtained after filtering and sorting.
     * @param filterExpr The filter expression of the particular panel.
     * @return The count value in the qualifier
     */
    private int determineCount(List<TurboIssue> issueList, FilterExpression filterExpr){
        for (Qualifier metaQualifier : filterExpr.find(Qualifier::isMetaQualifier)) {
            if (metaQualifier.getType() == QualifierType.COUNT && metaQualifier.getNumber().isPresent()) {
                return metaQualifier.getNumber().get();
            }
        }
        return issueList.size();
    }
}
