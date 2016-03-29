package ui;

import filter.expression.FilterExpression;
import javafx.application.Platform;
import javafx.scene.control.Label;
import ui.issuepanel.FilterPanel;
import ui.issuepanel.PanelControl;
import ui.issuepanel.UIBrowserBridge;
import util.DialogMessage;
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
    private String defaultRepoId;

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
        UI.events.registerEvent((UpdateRateLimitsEventHandler) this::updateAPIBox);
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

    private void updateAPIBox(UpdateRateLimitsEvent e) {
        Platform.runLater(() -> apiBox.setText(String.format("%s/%s",
                                                             e.remainingRequests,
                                                             Utility.minutesFromNow(e.nextRefreshInMillisecs)))
        );
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
