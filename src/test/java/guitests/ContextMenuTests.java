package guitests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.KeyCode;
import ui.components.FilterTextField;
import ui.listpanel.ListPanel;

public class ContextMenuTests extends UITest {

    private static final int EVENT_DELAY = 1000;
    private static final int DIALOG_DELAY = 1500;

    private ListPanel issuePanel;

    @Before
    public void setupUIComponent() {
        Platform.runLater(getStage()::show);
        Platform.runLater(getStage()::requestFocus);

        FilterTextField filterTextField = getFilterTextFieldAtPanel(0);
        filterTextField.setText("");
        Platform.runLater(filterTextField::requestFocus);

        clickOn(filterTextField);
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        issuePanel = getPanel(0);
    }

    /**
     * Tests context menu when no item is selected
     * All menu items should be disabled
     */
    @Test
    public void contextMenuDisabling_noIssueInListView_contextMenuItemsDisabled() {
        clickFilterTextFieldAtPanel(0);
        type("asdf");
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        rightClickPanel(0);
        sleep(EVENT_DELAY);

        ContextMenu contextMenu = issuePanel.getContextMenu();
        // Problem verifying menu item disable state in headless testing mode
        // Instead checks for null on first menu item which happens when disabled
        assertNull(contextMenu.getItems().get(0).getText());
    }

    /**
     * Tests selecting "Mark as read" and "Mark as unread"
     * context menu items
     */
    @Test
    public void testMarkAsReadUnread() {
        clickIssue(0, 9);
        rightClickIssue(0, 9);
        sleep(EVENT_DELAY);
        clickMenuItem("Mark as read (E)");
        sleep(EVENT_DELAY);
        assertTrue(getIssueCell(0, 9).getIssue().isCurrentlyRead());

        clickIssue(0, 9);
        rightClickIssue(0, 9);
        sleep(EVENT_DELAY);
        clickMenuItem("Mark as unread (U)");
        sleep(EVENT_DELAY);
        assertFalse(getIssueCell(0, 9).getIssue().isCurrentlyRead());
    }

    /**
     * Tests selecting "Change labels" context menu item
     */
    @Test
    public void testChangeLabels() {
        clickIssue(0, 9);
        rightClickIssue(0, 9);
        sleep(EVENT_DELAY);
        clickMenuItem("Change labels (L)");
        sleep(DIALOG_DELAY);

        assertNotNull(getLabelPickerTextField());

        push(KeyCode.ESCAPE);
        sleep(EVENT_DELAY);
    }

    /**
     * Tests selecting "Change milestone" context menu item
     */
    @Test
    public void contextMenu_selectChangeMilestoneMenu_successful() {
        rightClickIssue(0, 9);
        sleep(EVENT_DELAY);
        clickMenuItem("Change milestone (M)");
        sleep(DIALOG_DELAY);

        assertNotNull(getMilestonePickerTextField());

        push(KeyCode.ESCAPE);
        sleep(EVENT_DELAY);
    }

    /**
     * Tests selecting "Close issue" and "Reopen issue"
     */
    @Test
    public void testCloseReopenIssue() {
        rightClickIssue(0, 9);
        sleep(EVENT_DELAY);
        clickMenuItem("Close issue (C)");
        sleep(EVENT_DELAY);
        waitUntilNodeAppears("OK");
        clickOn("OK");
        sleep(EVENT_DELAY);
        waitUntilNodeAppears("Undo");
        clickOn("Undo");
        sleep(EVENT_DELAY);

        rightClickIssue(0, 6);
        sleep(EVENT_DELAY);
        clickMenuItem("Reopen issue (O)");
        sleep(EVENT_DELAY);
        waitUntilNodeAppears("OK");
        clickOn("OK");
        sleep(EVENT_DELAY);
        waitUntilNodeAppears("Undo");
        clickOn("Undo");
        sleep(EVENT_DELAY);
    }

    /**
     * Clicks on menu item with target text
     * @param menu
     * @param target
     */
    private void clickMenuItem(String target) {
        clickMenuItem(issuePanel.getContextMenu(), target);
    }
}
