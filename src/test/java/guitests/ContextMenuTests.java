package guitests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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
        FilterTextField filterTextField = find("#dummy/dummy_col0_filterTextField");
        filterTextField.setText("");
        click("#dummy/dummy_col0_filterTextField");
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
    }

    @Test
    public void testNoIssueSelected() {
        ListPanel issuePanel = find("#dummy/dummy_col0");

        click("#dummy/dummy_col0_filterTextField");
        type("asdf");
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        rightClick("#dummy/dummy_col0");

        ContextMenu contextMenu = issuePanel.getContextMenu();
        MenuItem readUnreadItem = contextMenu.getItems().get(0);
        MenuItem changeLabelsItem = contextMenu.getItems().get(1);

        assertTrue(readUnreadItem.isDisable());
        assertTrue(changeLabelsItem.isDisable());
    }

    @Test
    public void testSelectMarkAsReadUnRead() {
        ListPanelCell listPanelCell = find("#dummy/dummy_col0_9");

        click("#dummy/dummy_col0_9");
        rightClick("#dummy/dummy_col0_9");
        click("Mark as read (E)");
        sleep(EVENT_DELAY);
        assertTrue(listPanelCell.getIssue().isCurrentlyRead());

        click("#dummy/dummy_col0_9");
        rightClick("#dummy/dummy_col0_9");
        click("Mark as unread (U)");
        sleep(EVENT_DELAY);
        assertFalse(listPanelCell.getIssue().isCurrentlyRead());
    }

    @Test
    public void testSelectChangeLabels() {
        click("#dummy/dummy_col0_9");
        rightClick("#dummy/dummy_col0_9");
        click("Change labels (L)");
        sleep(DIALOG_DELAY);

        assertNotNull(find("#labelPickerTextField"));

        push(KeyCode.ESCAPE);
        sleep(EVENT_DELAY);
    }
}
