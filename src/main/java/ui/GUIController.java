package ui;

import ui.issuecolumn.ColumnControl;
import ui.issuecolumn.IssueColumn;
import ui.issuecolumn.UIBrowserBridge;
import ui.issuepanel.IssuePanel;
import util.events.ModelUpdatedEventHandler;

/**
 * This class manages the state of UI components and acts as a gateway between
 * back-end components and GUI components. Any mutation of GUI components should be
 * done here.
 */

public class GUIController {
    private ColumnControl columnControl;
    private UI ui;

    public GUIController(UI ui, ColumnControl columnControl) {
        this.ui = ui;
        this.columnControl = columnControl;

        // Set up the connection to the browser
        new UIBrowserBridge(ui);

        // Then register update events
        registerEvents();
    }

    public void registerEvents() {
        UI.events.registerEvent((ModelUpdatedEventHandler) e -> {
            columnControl.updateModel(e.model);
            columnControl.forEach(child -> {
               if (child instanceof IssuePanel) {
                   ((IssueColumn) child).setItems(e.model.getIssues(), e.hasMetadata);
               }
            });
        });
    }
}
