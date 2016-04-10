package guitests;

import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;

import org.junit.Before;
import org.junit.Test;

import ui.IdGenerator;
import ui.components.FilterTextField;
import ui.listpanel.ListPanel;
import util.PlatformEx;

import java.util.Optional;

import static org.junit.Assert.*;


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
        PlatformEx.waitOnFxThread(); // wait for traverseContextMenu's action to be carried out
        PlatformEx.waitOnFxThread(); // wait for panel refresh caused by mark as read
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

    @Test
    public void addToWatchListPanel_createNewWatchListPanel_issueAddedToNewPanel() {
        ListPanel listPanel = getPanel(0);
        clickIssue(0, 9);
        traverseContextMenu(listPanel.getContextMenu(), "Add to watch list", "New watch list");
        waitUntilNodeAppears("Cancel");

        type("Watch List #1");
        press(KeyCode.ENTER);

        ListPanel watchList1 = getPanel(1);
        assertEquals("Watch List #1", watchList1.getCurrentInfo().getPanelName());
        assertEquals("id:dummy/dummy#9", watchList1.getCurrentInfo().getPanelFilter());

        // clean up
        clickPanel(1);
        traverseHubTurboMenu("Panels", "Close");
    }

    @Test
    public void addToWatchListPanel_addToExistingWatchListPanel_issueAddedToExistingPanel() {
        traverseHubTurboMenu("Panels", "Create");
        PlatformEx.waitOnFxThread();
        ListPanel createdPanel = getPanel(1);
        createdPanel.setPanelName("New Panel");
        clickIssue(0, 11);
        ListPanel originalPanel = getPanel(0);
        traverseContextMenu(originalPanel.getContextMenu(), "Add to watch list", "New Panel");
        PlatformEx.waitOnFxThread();
        assertEquals("id:dummy/dummy#11", createdPanel.getCurrentInfo().getPanelFilter());

        // clean up
        clickOn(IdGenerator.getPanelFilterTextFieldIdReference(1));
        traverseHubTurboMenu("Panels", "Close");
    }

    @Test
    public void addToWatchListPanel_issueFromDifferentRepos_addedIssuesRetainRepoInfo() {
        clickIssue(0, 9);
        ListPanel listPanel = findOrWaitFor(IdGenerator.getPanelIdReference(0));
        traverseContextMenu(listPanel.getContextMenu(), "Add to watch list", "New watch list");
        waitUntilNodeAppears("Cancel");
        type("Watch List #1");
        press(KeyCode.ENTER);

        clickFilterTextFieldAtPanel(0);
        type("repo:dummy2/dummy2").push(KeyCode.ENTER);

        clickIssue(0, 10);
        traverseContextMenu(listPanel.getContextMenu(), "Add to watch list", "Watch List #1");
        PlatformEx.waitOnFxThread();

        ListPanel watchList1 = findOrWaitFor(IdGenerator.getPanelIdReference(1));
        assertEquals("id:dummy/dummy#9;dummy2/dummy2#1" +
                "0", watchList1.getCurrentInfo().getPanelFilter());

        // clean up
        clickPanel(1);
        traverseHubTurboMenu("Panels", "Close");
    }

    @Test
    public void editFilter_becomesValidWatchListPanel_panelAddedToContextMenu() {
        ListPanel originalPanel = getPanel(0);
        String originalPanelName = originalPanel.getCurrentInfo().getPanelName();

        clickOn(IdGenerator.getPanelFilterTextFieldIdReference(0));
        type("repo:dummy/dummy").push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        waitUntilNodeAppears(IdGenerator.getPanelCellIdReference(0, 1));
        clickOn(IdGenerator.getPanelCellIdReference(0, 1));
        ContextMenu contextMenu = originalPanel.getContextMenu();
        MenuItem addToWatchList = contextMenu.getItems().stream()
                                  .filter(menu -> menu.getText().equals("Add to watch list"))
                                  .findFirst()
                                  .get();
        Menu addToWatchListMenu = (Menu) addToWatchList;
        Optional<MenuItem> currentPanel = addToWatchListMenu.getItems().stream()
                                          .filter(menuItem -> menuItem.getText().equals(originalPanelName))
                                          .findFirst();
        assertFalse(currentPanel.isPresent());

        clickOn(IdGenerator.getPanelFilterTextFieldIdReference(0));
        selectAll();
        type("id:dummy/dummy#1").push(KeyCode.ENTER);
        clickOn(IdGenerator.getPanelCellIdReference(0, 1));
        contextMenu = originalPanel.getContextMenu();
        addToWatchList = contextMenu.getItems().stream()
                         .filter(menu -> menu.getText().equals("Add to watch list"))
                         .findFirst()
                         .get();
        addToWatchListMenu = (Menu) addToWatchList;
        currentPanel = addToWatchListMenu.getItems().stream()
                       .filter(menuItem -> menuItem.getText().equals(originalPanelName))
                       .findFirst();
        assertTrue(currentPanel.isPresent());
    }

}
