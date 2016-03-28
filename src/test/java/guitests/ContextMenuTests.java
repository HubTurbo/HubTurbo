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
import ui.listpanel.ListPanelCell;
import util.PlatformEx;

public class ContextMenuTests extends UITest {

    private static final int EVENT_DELAY = 1000;

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
        isDisabledContextMenu(contextMenu);
    }

    /**
     * Tests selecting "Mark as read" and "Mark as unread"
     * context menu items
     */
    @Test
    public void testMarkAsReadUnread() {
        ListPanel listPanel = getPanel(0);

        clickIssue(0, 9);
        traverseContextMenu(listPanel.getContextMenu(), "Mark as read (E)");
        PlatformEx.waitOnFxThread();
        assertTrue(getIssueCell(0, 9).getIssue().isCurrentlyRead());

        clickIssue(0, 9);
        traverseContextMenu(listPanel.getContextMenu(), "Mark as unread (U)");
        PlatformEx.waitOnFxThread();
        assertFalse(getIssueCell(0, 9).getIssue().isCurrentlyRead());
    }

    /**
     * Tests selecting "Change labels" context menu item
     */
    @Test
    public void testChangeLabels() {
        ListPanel listPanel = getPanel(0);

        clickIssue(0, 9);

        traverseContextMenu(listPanel.getContextMenu(), "Change labels (L)");
        PlatformEx.waitOnFxThread();

        assertNotNull(findOrWaitFor("#labelPickerTextField"));

        push(KeyCode.ESCAPE);
        sleep(EVENT_DELAY);
    }

    /**
     * Tests selecting "Change milestone" context menu item
     */
    @Test
    public void contextMenu_selectChangeMilestoneMenu_successful() {
        ListPanel listPanel = getPanel(0);

        clickIssue(0, 9);
        traverseContextMenu(listPanel.getContextMenu(), "Change milestone (M)");
        PlatformEx.waitOnFxThread();

        assertNotNull(findOrWaitFor("#milestonePickerTextField"));

        push(KeyCode.ESCAPE);
        sleep(EVENT_DELAY);
    }

    /**
     * Tests selecting "Close issue" and "Reopen issue"
     */
    @Test
    public void testCloseReopenIssue() {
        ListPanel listPanel = getPanel(0);

        clickIssue(0, 9);
        traverseContextMenu(listPanel.getContextMenu(), "Close issue (C)");
        PlatformEx.waitOnFxThread();
        waitUntilNodeAppears("OK");
        clickOn("OK");
        sleep(EVENT_DELAY);
        waitUntilNodeAppears("Undo");
        clickOn("Undo");
        sleep(EVENT_DELAY);

        clickIssue(0, 6);
        traverseContextMenu(listPanel.getContextMenu(), "Reopen issue (O)");
        PlatformEx.waitOnFxThread();
        waitUntilNodeAppears("OK");
        clickOn("OK");
        sleep(EVENT_DELAY);
        waitUntilNodeAppears("Undo");
        clickOn("Undo");
        sleep(EVENT_DELAY);
    }

    /**
     * Verifies that context menu is disabled
     * contextMenu.isDisabled() not used because of unreliability in headless test environment
     * @param contextMenu
     */
    private void isDisabledContextMenu(ContextMenu contextMenu) {
        assertNull(contextMenu.getItems().get(0).getText());
    }

}
