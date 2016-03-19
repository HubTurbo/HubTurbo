package guitests;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import org.junit.Before;
import org.junit.Test;
import ui.IdGenerator;
import ui.UI;
import ui.listpanel.ListPanel;
import util.events.IssueSelectedEventHandler;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IssueSelectedEventTest extends UITest {

    @Test
    public void eventTriggerOnIssueSelection_byRightClick_selectionEventNotTriggered() {
        String panelId = IdGenerator.getPanelIdForTest("dummy/dummy", 0);
        String cellId = IdGenerator.getPanelCellIdForTest("dummy/dummy", 0, 9);

        AtomicInteger eventCount = new AtomicInteger(0);
        UI.events.registerEvent((IssueSelectedEventHandler) e -> eventCount.incrementAndGet());
        ListPanel issuePanel = find(panelId);

        //testing whether right click occurred by checking the presence of context menu items
        rightClick(cellId);
        ContextMenu contextMenu = issuePanel.getContextMenu();
        for (MenuItem menuItem : contextMenu.getItems()) {
            assertTrue(!menuItem.isDisable());
        }

        // testing IssueSelectedEvent not registered on right click
        assertEquals(0, eventCount.get());
    }

    /**
     * Tests whether left click and key press triggers IssueSelectedEvent
     */
    @Test
    public void triggerIssueSelectedOnLeftClickAndKey_IssueInPanelLeftClickedAndKeyed_IssueSelectedTriggered() {
        String cellId = IdGenerator.getPanelCellIdForTest("dummy/dummy", 0, 10);

        AtomicInteger eventCount = new AtomicInteger(0);
        UI.events.registerEvent((IssueSelectedEventHandler) e -> eventCount.incrementAndGet());

        click(cellId);

        // testing IssueSelectedEvent is triggered on left click
        assertEquals(1, eventCount.get());

        //testing IssueSelectedEvent is triggered on key down to a particular issue
        push(KeyCode.DOWN);
        assertEquals(2, eventCount.get());
    }

}
