package unstable;

import guitests.UITest;
import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import org.junit.Before;
import org.junit.Test;
import ui.components.FilterTextField;
import ui.components.IssueListView;
import ui.listpanel.ListPanel;
import ui.listpanel.ListPanelCell;
import java.lang.reflect.*;

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
    public void markAllBelowAsReadUnread_twelveIssuesInListView_issuesCorrectlyMarkedReadUnread()
            throws NoSuchFieldException, IllegalAccessException {

        ListPanel issuePanel = find(PANEL_IDENTIFIER);
        Field listViewField = ListPanel.class.getDeclaredField("listView");
        listViewField.setAccessible(true);
        IssueListView listViewValue = (IssueListView) listViewField.get(issuePanel);

        // scrolls to the end of the panel
        listViewValue.scrollAndShow(12);


        //checking for issue #7 and below twice to make sure issues are marked correctly for read/unread cases
        markAndVerifyIssuesBelow(issuePanel, 7, true);
        verifyReadStatusOfIssuesBelow(issuePanel, 7, true);
        markAndVerifyIssuesBelow(issuePanel, 7, true);
        verifyReadStatusOfIssuesBelow(issuePanel, 7, true);
        markAndVerifyIssuesBelow(issuePanel, 7, false);
        verifyReadStatusOfIssuesBelow(issuePanel, 7, false);
        markAndVerifyIssuesBelow(issuePanel, 7, false);
        verifyReadStatusOfIssuesBelow(issuePanel, 7, false);

        //checking for the last issue to ensure correct marking of issues on/below as read/unread when no issues below
        markAndVerifyIssuesBelow(issuePanel, 1, true);
        verifyReadStatusOfIssuesBelow(issuePanel, 1, true);
        markAndVerifyIssuesBelow(issuePanel, 1, false);
        verifyReadStatusOfIssuesBelow(issuePanel, 1, false);
    }

    /**
     * Marks issues in the panel from Issue index in the UI right to the end of the list as read/unread
     * @param isMarkAsRead If true, marks the selected issue and below as read
     *                     If false, marks the selected issue and below as unread
     * @param index The issue number in the panel
     */
    private void markAndVerifyIssuesBelow(ListPanel issuePanel, int index, boolean isMarkAsRead){
        click("#" + issuePanel.getId() + "_" + index);
        rightClick("#" + issuePanel.getId() + "_" + index);
        ContextMenu contextMenu = issuePanel.getContextMenu();
        for (MenuItem menuItem : contextMenu.getItems()){
            awaitCondition(menuItem::isVisible);
        }
        if (isMarkAsRead){
            click("Mark all below as read");
        } else {
            click("Mark all below as unread");
        }
    }

    /**
     * Tests whether list panel cells corresponding to a particular index and below are marked as read/unread
     */
    private void verifyReadStatusOfIssuesBelow(ListPanel issuePanel, int index, boolean isExpectedStatusRead){
        for (int i = index; i >= 1; i--){
            ListPanelCell listPanelCell = find("#" + issuePanel.getId() + "_" + index);
            assertEquals(listPanelCell.getIssue().isCurrentlyRead(), isExpectedStatusRead);
        }
    }
}
