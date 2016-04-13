package ui;

import filter.expression.FilterExpression;
import javafx.application.Platform;
import javafx.scene.control.Label;
import ui.issuepanel.FilterPanel;
import ui.issuepanel.PanelControl;
import ui.issuepanel.UIBrowserBridge;
import util.DialogMessage;
import util.RefreshTimer;
import util.Utility;
import util.events.*;
import util.events.testevents.PrimaryRepoChangedEvent;
import util.events.testevents.PrimaryRepoChangedEventHandler;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages the state of UI components and acts as a gateway between back-end components and
 * GUI components. Any mutation of GUI components should be carried out here.
 */

public class GUIController {

    private final PanelControl panelControl;
    private final UI ui;
    private final Label apiBox;

    /**
     * The previous amount of the available remaining API request.
     */
    private int previousRemainingApiRequests = 0;

    /**
     * The previous amount of api calls used.
     */
    private int apiCallsUsedInPreviousRefresh = 0;

    private String defaultRepoId;

    /**
     * The duration between each refresh of the data store.
     */
    private long refreshDurationInMinutes = 1;

    public GUIController(UI ui, PanelControl panelControl, Label apiBox) {
        this.ui = ui;
        this.panelControl = panelControl;
        this.apiBox = apiBox;

        // Set up the connection to the browser
        new UIBrowserBridge(ui);

        // Then register model update event handler
        registerEvents();
    }

    public final void registerEvents() {
        UI.events.registerEvent((ModelUpdatedEventHandler) this::modelUpdated);
        UI.events.registerEvent((RateLimitsUpdatedEventEventHandler) this::updateRateLimits);
        UI.events.registerEvent((RefreshTimerTriggeredEventHandler) this::updateSyncRefreshRate);
        UI.events.registerEvent((ShowErrorDialogEventHandler) this::showErrorDialog);
        UI.events.registerEvent((PrimaryRepoChangedEventHandler) this::setDefaultRepo);
    }

    /**
     * The handler method for a ModelUpdatedEvent.
     * <p>
     * It processes each panel in the current GUI, and checks the ModelUpdatedEvent for issues to be displayed
     * that match the current panel's filter expression:
     * - If not, the panel does not change its appearance.
     * - If there is a match, the panel's issue list is changed to the corresponding one contained in the
     * ModelUpdatedEvent.
     *
     * @param e The ModelUpdatedEvent triggered by the uiManager.
     */
    private void modelUpdated(ModelUpdatedEvent e) {
        panelControl.getChildren().stream()
                .filter(child -> child instanceof FilterPanel)
                .forEach(child -> {
                    // Search for the corresponding entry in e.issuesToShow.
                    List<GuiElement> filterResult =
                            e.elementsToShow.get(((FilterPanel) child).getCurrentFilterExpression());

                    if (filterResult != null) ((FilterPanel) child).updatePanel(filterResult);
                });
    }

    /**
     * Handler method for an applyFilterExpression call from an FilterPanel, which is in turn triggered by
     * the user pressing ENTER while the cursor is on the FilterPanel's filterTextField.
     * <p>
     * Triggers a processAndRefresh call in Logic with only the given panel's filterExpression. Contrast this
     * with refreshAllPanels in Logic, triggers processAndRefresh with all FilterExpressions from the GUI.
     *
     * @param changedPanel The panel whose filter expression had been changed by the user.
     */
    public void panelFilterExpressionChanged(FilterPanel changedPanel) {
        ui.logic.refreshPanel(changedPanel);
        UI.events.triggerEvent(new UsedReposChangedEvent());
    }

    /**
     * Called from uiManager to send all panels' filters to Logic.
     *
     * @return The filters contained in all currently displayed panels.
     */
    public List<FilterExpression> getAllFilters() {
        return panelControl.getChildren().stream()
                .filter(child -> child instanceof FilterPanel)
                .map(child -> ((FilterPanel) child).getCurrentFilterExpression())
                .collect(Collectors.toList());
    }

    public List<FilterPanel> getAllPanels() {
        return panelControl.getChildren().stream()
                .filter(child -> child instanceof FilterPanel)
                .map(child -> (FilterPanel) child)
                .collect(Collectors.toList());
    }

    /**
     * Updates components using the api rate limits information.
     * @param e The current api rate limits
     */
    private void updateRateLimits(RateLimitsUpdatedEvent e) {
        updateAPIBox(e.remainingRequests, e.nextRefreshInMillisecs);
    }

    /**
     * Updates the sync refresh rate of updating on the current data store.
     * @param e The current api rate limits for calculation of the refresh rate.
     */
    private void updateSyncRefreshRate(RefreshTimerTriggeredEvent e) {
        apiCallsUsedInPreviousRefresh = computePreviousRemainingApiRequests(e.remainingRequests);
        refreshDurationInMinutes = RefreshTimer.computeRefreshTimerPeriod(e.remainingRequests,
                                                                  Utility.minutesFromNow(e.nextRefreshInMillisecs),
                apiCallsUsedInPreviousRefresh, RefreshTimer.API_QUOTA_BUFFER,
                                                                  RefreshTimer.DEFAULT_REFRESH_PERIOD_IN_MINS);
        ui.refreshTimer.changeRefreshPeriod((int) refreshDurationInMinutes);
    }

    private int computePreviousRemainingApiRequests(int remainingRequests) {
        int difference = previousRemainingApiRequests - remainingRequests;
        previousRemainingApiRequests = remainingRequests;

        if (difference >= 0) {
            return difference;
        }
        return apiCallsUsedInPreviousRefresh;
    }

    /**
     * Updates the GUI APIBox to indicate the no of remaining api request, time until next api renewal and
     * the current sync refresh rate.
     * @param remainingRequests The number of remaining api request.
     * @param nextRefreshInMillisecs The next refresh time in epoch format.
     */
    private void updateAPIBox(int remainingRequests, long nextRefreshInMillisecs) {
        Platform.runLater(() -> apiBox.setText(String.format("%s/%s[x%d]", remainingRequests,
                                               Utility.minutesFromNow(nextRefreshInMillisecs),
                                               (int) Math.ceil(refreshDurationInMinutes))));
    }

    private void showErrorDialog(ShowErrorDialogEvent e) {
        Platform.runLater(() -> DialogMessage.showErrorDialog(e.header, e.message));
    }

    private void setDefaultRepo(PrimaryRepoChangedEvent e) {
        defaultRepoId = e.repoId;
    }

    public String getDefaultRepo() {
        return defaultRepoId;
    }
}
