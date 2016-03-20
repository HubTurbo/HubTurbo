package unstable;

import guitests.UITest;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import org.junit.Before;
import org.junit.Test;
import ui.IdGenerator;
import ui.components.FilterTextField;
import ui.components.IssueListView;
import ui.listpanel.ListPanel;
import ui.listpanel.ListPanelCell;
import util.PlatformEx;

import java.lang.reflect.*;

import static org.junit.Assert.assertEquals;

public class MarkAllIssuesAsReadUnreadTests extends UITest {

    private static final String REPO_ID = "dummy/dummy";
    private static final String PANEL_ID = IdGenerator.getPanelId(REPO_ID, 0);
    private static final String FILTER_TEXT_FIELD_ID = IdGenerator.getPanelFilterTextFieldIdForTest(REPO_ID, 0);

    @Before
    public void setup() {
        PlatformEx.runAndWait(stage::requestFocus);

        FilterTextField filterTextField = find(FILTER_TEXT_FIELD_ID);
        filterTextField.setText("");

        click(FILTER_TEXT_FIELD_ID);
        push(KeyCode.ENTER);
    }

    /**
     * Tests selecting "Mark all below as read" and "Mark all below as unread" context menu items
     */
    @Test
    public void markAllBelowAsReadUnread_multipleIssuesInListView_issuesCorrectlyMarked()
            throws NoSuchFieldException, IllegalAccessException {

        ListPanel issuePanel = find(PANEL_ID);
        Field listViewField = ListPanel.class.getDeclaredField("listView");
        listViewField.setAccessible(true);
        IssueListView listViewValue = (IssueListView) listViewField.get(issuePanel);

        // scrolls to the end of the panel
        listViewValue.scrollAndShow(issuePanel.getIssuesCount());


        //checking for issue #7 and below as marked read
        clickAndMarkIssuesBelow(issuePanel, 7, true);
        verifyReadStatusOfIssuesBelow(7, true);

        //checking for issue #7 and below as marked read again to ensure they remain read even on a repeated command
        clickAndMarkIssuesBelow(issuePanel, 7, true);
        verifyReadStatusOfIssuesBelow(7, true);

        //checking for issue #7 and below as marked unread
        clickAndMarkIssuesBelow(issuePanel, 7, false);
        verifyReadStatusOfIssuesBelow(7, false);

        //checking for issue #7 and below as marked unread again to ensure they remain unread
        // even on a repeated command
        clickAndMarkIssuesBelow(issuePanel, 7, false);
        verifyReadStatusOfIssuesBelow(7, false);

        //checking for the last issue as marked read
        clickAndMarkIssuesBelow(issuePanel, 1, true);
        verifyReadStatusOfIssuesBelow(1, true);

        //checking for the last issue as marked unread
        clickAndMarkIssuesBelow(issuePanel, 1, false);
        verifyReadStatusOfIssuesBelow(1, false);
    }

    /**
     * Marks issues in the panel from Issue index in the UI right to the end of the list as read/unread
     *
     * @param isMarkAsRead If true, marks the selected issue and below as read
     *                     If false, marks the selected issue and below as unread
     * @param index        The issue number in the panel
     */
    private void clickAndMarkIssuesBelow(ListPanel issuePanel, int index, boolean isMarkAsRead) {
        String panelCellId = IdGenerator.getPanelCellIdForTest(REPO_ID, 0, index);
        click(panelCellId);
        rightClick(panelCellId);
        ContextMenu contextMenu = issuePanel.getContextMenu();
        for (MenuItem menuItem : contextMenu.getItems()) {
            awaitCondition(menuItem::isVisible);
        }

        if (isMarkAsRead) {
            click(ListPanel.MARK_ALL_AS_READ_MENU_ITEM_TEXT);
        } else {
            click(ListPanel.MARK_ALL_AS_UNREAD_MENU_ITEM_TEXT);
        }
    }

    /**
     * Tests whether list panel cells corresponding to a particular index and below are marked as read/unread
     */
    private void verifyReadStatusOfIssuesBelow(int index, boolean isExpectedStatusRead) {
        for (int i = index; i >= 1; i--) {
            ListPanelCell listPanelCell = find(IdGenerator.getPanelCellIdForTest(REPO_ID, 0, index));
            assertEquals(listPanelCell.getIssue().isCurrentlyRead(), isExpectedStatusRead);
        }
    }
}
