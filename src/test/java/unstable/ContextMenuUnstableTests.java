package unstable;

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

import static org.junit.Assert.assertEquals;

public class ContextMenuUnstableTests extends UITest {

    private static final String PANEL_IDENTIFIER = "#dummy/dummy_col0";

    @Before
    public void setup() {
        Platform.runLater(stage::show);
        Platform.runLater(stage::requestFocus);

        FilterTextField filterTextField = find(PANEL_IDENTIFIER + "_filterTextField");
        filterTextField.setText("");
        Platform.runLater(filterTextField::requestFocus);

        click(PANEL_IDENTIFIER + "_filterTextField");
        push(KeyCode.ENTER);
    }

    /**
     * Tests selecting "Mark all below as read" and "Mark all below as unread" context menu items
     */
    @Test
    public void markAllBelowAsReadUnread_twelveIssuesInListView_issuesCorrectlyMarkedReadUnread() {
        ListPanel issuePanel = find(PANEL_IDENTIFIER);
        // scrolls to the end of the panel
        issuePanel.getListView().scrollAndShow(12);

        //checking for issue #7 and below twice to make sure issues are marked correctly for read/unread cases
        for (int i = 0; i < 2; i++) {
            markAndVerifyIssuesBelow(7, true);
        }
        for (int i = 0; i < 2; i++) {
            markAndVerifyIssuesBelow(7, false);
        }

        //checking for the last issue to ensure correct marking of issues on/below as read/unread when no issues below
        markAndVerifyIssuesBelow(1, true);
        markAndVerifyIssuesBelow(1, false);
    }

    /**
     * Marks and tests issues in a panel of 12 issues
     * from Issue #{index} in the UI right to the end of the list as read/unread
     * @param isMarkAsRead If true, tests whether issues on/below the selected issue are being correctly marked read
     *                      If false, tests whether issues on/below the selected are being correctly marked unread
     * @param index The issue number in the panel
     */
    private void markAndVerifyIssuesBelow(int index, boolean isMarkAsRead){
        ListPanel issuePanel = find(PANEL_IDENTIFIER);
        click(PANEL_IDENTIFIER + "_" + index);
        rightClick(PANEL_IDENTIFIER + "_" + index);
        ContextMenu contextMenu = issuePanel.getContextMenu();
        for (MenuItem menuItem : contextMenu.getItems()){
            awaitCondition(menuItem::isVisible);
        }
        if (isMarkAsRead){
            click("Mark all below as read");
        } else {
            click("Mark all below as unread");
        }
        for (int i = index; i >= 1; i--){
            verifyReadStatusOfIssue(i, isMarkAsRead);
        }
    }

    /**
     * Tests whether a list panel cell corresponding to a particular index is marked as read/unread
     */
    private void verifyReadStatusOfIssue(int index, boolean isExpectedStatusRead){
        ListPanelCell listPanelCell = find(PANEL_IDENTIFIER + "_" + index);
        assertEquals(listPanelCell.getIssue().isCurrentlyRead(), isExpectedStatusRead);
    }
}
