package unstable;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import guitests.UITest;
import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;

import org.junit.Before;
import org.junit.Test;

import ui.components.FilterTextField;
import ui.listpanel.ListPanel;
import ui.listpanel.ListPanelCell;


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
     * Tests selecting "Change labels" context menu item
     */
    @Test
    public void test3() {
        click("#dummy/dummy_col0_9");
        rightClick("#dummy/dummy_col0_9");
        sleep(EVENT_DELAY);
        click("Change labels (L)");
        sleep(DIALOG_DELAY);

        assertNotNull(find("#queryField"));

        push(KeyCode.ESCAPE);
        sleep(EVENT_DELAY);
    }

}
