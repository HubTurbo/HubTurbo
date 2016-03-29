package guitests;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import prefs.PanelInfo;
import ui.TestController;
import ui.UI;
import ui.issuepanel.PanelControl;
import util.PlatformEx;

import java.util.Optional;

import static ui.PanelMenuCreator.ASSIGNEE_FILTER_NAME;
import static ui.PanelMenuCreator.ASSIGNEE_PANEL_NAME;
import static ui.PanelMenuCreator.MILESTONE_FILTER_NAME;
import static ui.PanelMenuCreator.MILESTONE_PANEL_NAME;
import static ui.PanelMenuCreator.UPDATED_FILTER_NAME;
import static ui.PanelMenuCreator.UPDATED_PANEL_NAME;

public class PanelMenuCreatorTest extends UITest {

    private PanelControl panelControl;

    @Before
    public void setup() {
        UI ui = TestController.getUI();
        panelControl = ui.getPanelControl();
    }

    @Test
    public void assigneePanelMenuItemTest() {
        customizedPanelMenuItemTest(ASSIGNEE_PANEL_NAME, ASSIGNEE_FILTER_NAME);
    }

    @Test
    public void milestonePanelMenuItemTest() {
        customizedPanelMenuItemTest(MILESTONE_PANEL_NAME, MILESTONE_FILTER_NAME);
    }

    @Test
    public void recentlyUpdatedPanelMenuItemTest() {
        customizedPanelMenuItemTest(UPDATED_PANEL_NAME, UPDATED_FILTER_NAME);
    }

    @Test
    public void createPanelTest() {
        traverseMenu("Panels", "Create");

        waitAndAssertEquals(2, panelControl::getPanelCount);
        assertEquals(Optional.of(1), panelControl.getCurrentlySelectedPanel());

        traverseMenu("Panels", "Create (Left)");
        waitAndAssertEquals(3, panelControl::getPanelCount);
        assertEquals(Optional.of(0), panelControl.getCurrentlySelectedPanel());

        traverseMenu("Panels", "Close");
        traverseMenu("Panels", "Close");
    }

    private void customizedPanelMenuItemTest(String panelName, String panelFilter) {
        PlatformEx.waitOnFxThread();
        traverseMenu("Panels", "Auto-create", panelName);

        waitAndAssertEquals(2, panelControl::getPanelCount);
        assertEquals(Optional.of(1), panelControl.getCurrentlySelectedPanel());

        PanelInfo panelInfo = panelControl.getCurrentPanelInfos().get(1);
        waitAndAssertEquals(panelFilter, panelInfo::getPanelFilter);
        assertEquals(panelName, panelInfo.getPanelName());
        traverseMenu("Panels", "Close");
    }

}
