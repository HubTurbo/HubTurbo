package guitests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;

import org.junit.Before;
import org.junit.Test;

import ui.components.FilterTextField;
import ui.listpanel.ListPanel;
import ui.listpanel.ListPanelCell;
import util.PlatformEx;

public class ContextMenuTests extends UITest {

    private static final int EVENT_DELAY = 1000;
    private static final int DIALOG_DELAY = 1500;

    @Before
    public void setup() {
        Platform.runLater(stage::show);
        Platform.runLater(stage::requestFocus);

        FilterTextField filterTextField = find("#dummy/dummy_col0_filterTextField");
        filterTextField.setText("");
        Platform.runLater(filterTextField::requestFocus);

        click("#dummy/dummy_col0_filterTextField");
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
    }

    /**
     * Tests context menu when no item is selected
     * All menu items should be disabled
     */
    @Test
    public void contextMenuDisabling_noIssueInListView_contextMenuItemsDisabled() {
        ListPanel issuePanel = find("#dummy/dummy_col0");

        click("#dummy/dummy_col0_filterTextField");
        type("asdf");
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        rightClick("#dummy/dummy_col0");
        sleep(EVENT_DELAY);

        ContextMenu contextMenu = issuePanel.getContextMenu();
        for (MenuItem menuItem : contextMenu.getItems()){
            assertTrue(menuItem.isDisable());
        }
    }

    /**
     * Tests selecting "Mark as read" and "Mark as unread"
     * context menu items
     */
    @Test
    public void test2() {
        ListPanelCell listPanelCell = find("#dummy/dummy_col0_9");

        click("#dummy/dummy_col0_9");
        rightClick("#dummy/dummy_col0_9");
        sleep(EVENT_DELAY);
        click("Mark as read (E)");
        sleep(EVENT_DELAY);
        assertTrue(listPanelCell.getIssue().isCurrentlyRead());

        click("#dummy/dummy_col0_9");
        rightClick("#dummy/dummy_col0_9");
        sleep(EVENT_DELAY);
        click("Mark as unread (U)");
        sleep(EVENT_DELAY);
        assertFalse(listPanelCell.getIssue().isCurrentlyRead());
    }

    /**
     * Tests selecting "Mark all below as read" and "Mark all below as unread" context menu items
     */
    @Test
    public void markAllBelowAsReadUnread_tenIssuesInListView_markIssue9andBelowReadUnread() {
        markAllReadOrUnread(true);
        markAllReadOrUnread(false);
    }

    /**
     * Tests selecting "Change labels" context menu item
     */
    @Test
    public void test3() {
        click("#dummy/dummy_col0_9");
        rightClick("#dummy/dummy_col0_9");
        sleep(EVENT_DELAY);
        click("Change labels (L)");
        sleep(DIALOG_DELAY);

        assertNotNull(find("#labelPickerTextField"));

        push(KeyCode.ESCAPE);
        sleep(EVENT_DELAY);
    }

    /**
     * Tests marked issues from Issue #9 in the UI right to the end of the list as read/unread
     * @param isTestingRead If true, tests whether issues on/below the selected issue are being correctly marked read
     *                      If false, tests whether issues on/below the selected are being correctly marked unread
     */
    private void markAllReadOrUnread(boolean isTestingRead){
        click("#dummy/dummy_col0_9");
        rightClick("#dummy/dummy_col0_9");
        if (isTestingRead){
            click("Mark all below as read");
        } else {
            click("Mark all below as unread");
        }
        for (int i = 9; i >= 1; i--){
            ListPanelCell listPanelCell = find("#dummy/dummy_col0_" + i);
            if (isTestingRead){
                assertTrue(listPanelCell.getIssue().isCurrentlyRead());
            } else {
                assertFalse(listPanelCell.getIssue().isCurrentlyRead());
            }
        }
    }

}
