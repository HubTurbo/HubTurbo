package ui;

import backend.interfaces.IModel;
import backend.resource.TurboIssue;
import filter.expression.FilterExpression;
import filter.expression.Qualifier;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.collections.transformation.TransformationList;
import javafx.scene.control.Label;
import ui.issuepanel.FilterPanel;
import ui.issuepanel.PanelControl;
import ui.issuepanel.UIBrowserBridge;
import util.DialogMessage;
import util.Utility;
import util.events.*;

import java.util.*;
import java.util.function.Predicate;

/**
 * Manages the state of UI components and acts as a gateway between back-end components and
 * GUI components. Any mutation of GUI components should be carried out here.
 */

public class GUIController {
    private PanelControl panelControl;
    private UI ui;
    private Label apiBox;

    // Synchronised with Logic's latest updated multimodel.
    // Not to be modified from this point or any further in the GUI.
    private IModel multiModel;

    public GUIController(UI ui, PanelControl panelControl, Label apiBox) {
        this.ui = ui;
        this.panelControl = panelControl;
        this.apiBox = apiBox;

        // Set up the connection to the browser
        new UIBrowserBridge(ui);

        // Then register model update event handler
        registerEvents();
    }

    public void registerEvents() {
        UI.events.registerEvent((ModelUpdatedEventHandler) this::modelUpdated);
        UI.events.registerEvent((UpdateRateLimitsEventHandler) this::updateAPIBox);
        UI.events.registerEvent((ShowErrorDialogEventHandler) this::showErrorDialog);
    }

    /**
     * The handler method for a ModelUpdatedEvent. It extracts the issues of the multimodel carried by the event,
     * and then filters and sorts the issues to place each panel in PanelControl based on these issues.
     *
     * The filtering process also produces a list of issues to request metadata for, if e.hasMetadata is false
     * and their respective panels specify the display of metadata (through the UPDATED filter).
     *
     * Based on the hasMetadata state of the model, as specified by the event, the issues will then be displayed
     * on their respective panels, or held back until their metadata requests have been fired and the downloaded
     * metadata come back as a subsequent ModelUpdatedEvent (with e.hasMetadata being true).
     *
     * @param e The ModelUpdatedEvent triggered by the uiManager.
     */
    private void modelUpdated(ModelUpdatedEvent e) {
        multiModel = e.model;

        // Use updatedModel while handling a ModelUpdatedEvent to avoid race conditions.
        IModel updatedModel = e.model;

        // Set the new model to panelControl. This is in turn passed from PanelControl to ListPanel,
        // down to each ListPanelCard in order to display details about each issue such as labels and assignees.
        panelControl.updateModel(updatedModel);

        // Extracts all issues from the multimodel. This is then filtered through each of the panels' filters
        // to produce the appropriate list of issues to be displayed.
        ObservableList<TurboIssue> allModelIssues = FXCollections.observableArrayList(updatedModel.getIssues());

        // Populated in processPanel calls.
        HashMap<String, HashSet<Integer>> toUpdate = new HashMap<>();

        panelControl.getChildren().forEach(child -> {
            if (child instanceof FilterPanel) {
                processPanel((FilterPanel) child, updatedModel, allModelIssues, toUpdate, e.hasMetadata);
            }
        });

        // If toUpdate is empty, no metadata is requested.
        dispatchMetadataRequests(toUpdate);
    }

    /**
     * Handler method for an applyFilterExpression call from an FilterPanel, which is in turn triggered by
     * the user pressing ENTER while the cursor is on the FilterPanel's filterTextField.
     *
     * The logic in this method is similar to that of modelUpdated, but only the FilterPanel whose
     * filterTextField was changed will be processed.
     *
     * The multiModel to use here is stored from the last time a ModelUpdatedEvent was triggered.
     *
     * @param changedPanel The panel whose filter expression had been changed by the user.
     */
    public void panelFilterExpressionChanged(FilterPanel changedPanel) {
        ObservableList<TurboIssue> allModelIssues = FXCollections.observableArrayList(multiModel.getIssues());
        HashMap<String, HashSet<Integer>> toUpdate = new HashMap<>();

        // This is not triggered by a (metadata) update, so we pass false into the call.
        processPanel(changedPanel, multiModel, allModelIssues, toUpdate, false);

        dispatchMetadataRequests(toUpdate);
    }

    /**
     * Manages the flow of execution in filtering and updating a panel.
     *
     * It opens all necessary repos, then filters issues. Then, it determines whether to refresh the items
     * on the issue panel, thus presenting the new data to the user, or to hold off this data and instead fire
     * off metadata requests.
     *
     * updatedModel and allModelIssues are specified as separate arguments as the extraction of allModelIssues
     * is O(n).
     *
     * @param panelToProcess The panel whose filter expression will be used to filter issues.
     * @param updatedModel The model whose data will be used to display issue details.
     * @param allModelIssues The list of issues extracted from the model.
     * @param toUpdate The tally for metadata requests. Ignored if the issues already have metadata, or don't need it.
     * @param isMetadataUpdate Determines whether issues have the necessary metadata to be displayed to the user.
     */
    public void processPanel(FilterPanel panelToProcess,
                             IModel updatedModel,
                             ObservableList<TurboIssue> allModelIssues,
                             HashMap<String, HashSet<Integer>> toUpdate,
                             boolean isMetadataUpdate) {

        // Extract the filter expression and the meta qualifiers within it. The expression is used for
        // filtering the issues, whereas the meta qualifiers are used for a special issue sorting order,
        // as well as to determine whether to tally issues for metadata updates.
        FilterExpression panelExpression = panelToProcess.getCurrentFilterExpression();
        List<Qualifier> panelMetaQualifiers = panelExpression.find(Qualifier::isMetaQualifier);

        // First we request all necessary repos. The opened repos will come in subsequent ModelUpdatedEvents.
        openAllReposInExpression(panelMetaQualifiers);

        boolean hasUpdatedQualifier = updatedQualifierExists(panelMetaQualifiers);

        // Issues are filtered and sorted here. isMetadataUpdate and hasUpdatedQualifier are used to determine
        // whether to use an implicit non-self-update sorting order.
        TransformationList<TurboIssue, TurboIssue> filteredAndSortedIssues =
                filterAndSortPanel(panelExpression, panelMetaQualifiers,
                        updatedModel, allModelIssues, isMetadataUpdate && hasUpdatedQualifier);

        // If the filter expression has an UPDATED qualifier, we must ensure that the issues have the relevant
        // metadata before showing them to the user. If not, we only tally the issues up for metadata update.
        // However, even if this is not a metadata update, but there are no issues to display or retrieve metadata
        // for, we also display the empty panel to the user.
        if (!hasUpdatedQualifier || isMetadataUpdate || filteredAndSortedIssues.isEmpty()) {
            updatePanel(panelToProcess, filteredAndSortedIssues, isMetadataUpdate);
        } else {
            populateUpdateList(filteredAndSortedIssues, toUpdate);
        }
    }

    /**
     * Produces a list of issues, filtered and sorted from all issues from the given multimodel, based on
     * the given filter expression.
     *
     * Like updatedModel and allModelIssues, panelExpression and panelMetaQualifiers are passed separately
     * as extraction of the meta qualifiers is O(n).
     *
     * @param panelExpression The filter expression belonging to the panel.
     * @param panelMetaQualifiers The meta qualifiers in the panel's filter expression.
     * @param updatedModel The model to be used to display issue details such as assignee and labels.
     * @param allModelIssues The list of issues extracted from the model.
     * @param isSortableByNonSelfUpdates Determines the behaviour of the sort key "nonSelfUpdate".
     * @return The list of filtered and sorted issues for the panel.
     */
    private TransformationList<TurboIssue, TurboIssue> filterAndSortPanel(FilterExpression panelExpression,
                                                                          List<Qualifier> panelMetaQualifiers,
                                                                          IModel updatedModel,
                                                                          ObservableList<TurboIssue> allModelIssues,
                                                                          boolean isSortableByNonSelfUpdates) {

        Predicate<TurboIssue> predicate = issue -> Qualifier.process(updatedModel, panelExpression, issue);
        Comparator<TurboIssue> comparator = determineComparator(panelMetaQualifiers, isSortableByNonSelfUpdates);

        return new SortedList<>(new FilteredList<>(allModelIssues, predicate), comparator);
    }

    /**
     * Produces a suitable comparator based on the given data.
     *
     * @param panelMetaQualifiers The given meta qualifiers, from which Sort qualifiers will be processed.
     * @param isSortableByNonSelfUpdates Determines the behaviour of the sort key "nonSelfUpdate".
     * @return The comparator to use.
     */
    private Comparator<TurboIssue> determineComparator(List<Qualifier> panelMetaQualifiers,
                                                       boolean isSortableByNonSelfUpdates) {

        for (Qualifier metaQualifier : panelMetaQualifiers) {
            // Only take into account the first sort qualifier found
            if (metaQualifier.getName().equals("sort")) {
                return metaQualifier.getCompoundSortComparator(multiModel, isSortableByNonSelfUpdates);
            }
        }

        // No sort qualifier, look for updated qualifier
        if (isSortableByNonSelfUpdates) {
            return Qualifier.getSortComparator(multiModel, "nonSelfUpdate", true, true);
        }

        // No sort or updated, return sort by descending ID, which is the default.
        return Qualifier.getSortComparator(multiModel, "id", true, false);
    }

    /**
     * Triggers multiple calls in ui.logic to open all repositories mentioned in a given filter expression,
     * whose meta qualifiers are passed into the function.
     *
     * @param panelMetaQualifiers Meta qualifiers from which repo qualifiers will be extracted.
     */
    private void openAllReposInExpression(List<Qualifier> panelMetaQualifiers) {
        panelMetaQualifiers.forEach(metaQualifier -> {
            if (metaQualifier.getName().equals(Qualifier.REPO) && metaQualifier.getContent().isPresent()) {
                ui.logic.openRepositoryFromFilter(metaQualifier.getContent().get());
            }
        });
    }

    /**
     * Presents the given list of issues to the user by placing them on the given panel and refreshing the panel,
     * resulting in actual GUI elements being created from the given issues.
     *
     * @param panelToUpdate The panel to display the issues on.
     * @param filteredAndSortedIssues The issues to be displayed.
     * @param isMetadataUpdate Determines whether comment bubbles will be highlighted based on non-self update times.
     */
    private static void updatePanel(FilterPanel panelToUpdate,
                                    TransformationList<TurboIssue, TurboIssue> filteredAndSortedIssues,
                                    boolean isMetadataUpdate) {
        panelToUpdate.setIssueList(filteredAndSortedIssues);
        panelToUpdate.refreshItems(isMetadataUpdate);
    }

    /**
     * Mutates the issuesToUpdate HashMap by adding all issues in filteredAndSortedIssues.
     *
     * The usage of HashMap and HashSet ensures that there will be no duplicate metadata requests sent,
     * hence reducing the number of API calls made.
     *
     * @param filteredAndSortedIssues Issues to populate the HashMap.
     * @param issuesToUpdate The HashMap to be populated.
     */
    private static void populateUpdateList(TransformationList<TurboIssue, TurboIssue> filteredAndSortedIssues,
                                           HashMap<String, HashSet<Integer>> issuesToUpdate) {

        for (TurboIssue issueToUpdate : filteredAndSortedIssues) {
            // Retrieve to check if the HashSet representing the issue's repo already exists.
            HashSet<Integer> issuesInRepo = issuesToUpdate.get(issueToUpdate.getRepoId());
            if (issuesInRepo != null) {
                // If yes, just add the issue.
                issuesInRepo.add(issueToUpdate.getId());
            } else {
                // If no, initialize the HashSet first, then add the issue.
                issuesInRepo = new HashSet<>();
                issuesInRepo.add(issueToUpdate.getId());
                issuesToUpdate.put(issueToUpdate.getRepoId(), issuesInRepo);
            }
        }
    }


    /**
     * Triggers metadata requests based on the given HashMap.
     *
     * @param toUpdate The HashMap containing issues for which to get metadata.
     */
    private void dispatchMetadataRequests(HashMap<String, HashSet<Integer>> toUpdate) {
        toUpdate.entrySet().forEach(repoSetEntry ->
                        ui.logic.getIssueMetadata(repoSetEntry.getKey(), new ArrayList<>(repoSetEntry.getValue()))
        );
    }

    /**
     * Auxiliary method to search for the UPDATED qualifier in a given FilterExpression's meta qualifier list.
     *
     * @param panelMetaQualifiers The meta qualifiers from which to search for UPDATED qualifiers.
     * @return Whether the given meta qualifiers contain an UPDATED qualifier.
     */
    private static boolean updatedQualifierExists(List<Qualifier> panelMetaQualifiers) {
        for (Qualifier metaQualifier : panelMetaQualifiers) {
            // Only take into account the first updated qualifier
            if (metaQualifier.getName().equals("updated")) return true;
        }
        return false;
    }

    private void updateAPIBox(UpdateRateLimitsEvent e) {
        Platform.runLater(() -> apiBox.setText(String.format("%s/%s",
                    e.remainingRequests,
                    Utility.minutesFromNow(e.nextRefreshInMillisecs)))
        );
    }

    private void showErrorDialog(ShowErrorDialogEvent e) {
        Platform.runLater(() -> DialogMessage.showErrorDialog(e.header, e.message));
    }
}
