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

public class PanelMenuCreatorTest extends UITest{

    private PanelControl panelControl;

    @Before
    public void setup() {
        UI ui = TestController.getUI();
        panelControl = ui.getPanelControl();
    }

    @Test
    public void assigneePanelMenuItemTest(){
        customizedPanelMenuItemTest(ASSIGNEE_PANEL_NAME, ASSIGNEE_FILTER_NAME);
    }

    @Test
    public void milestonePanelMenuItemTest(){
        customizedPanelMenuItemTest(MILESTONE_PANEL_NAME, MILESTONE_FILTER_NAME);
    }

    @Test
    public void recentlyUpdatedPanelMenuItemTest(){
        customizedPanelMenuItemTest(UPDATED_PANEL_NAME, UPDATED_FILTER_NAME);
    }

    @Test
    public void createPanelTest(){
        clickMenu("Panels", "Create");

        assertEquals(panelControl.getPanelCount(), 2);
        assertEquals(panelControl.getCurrentlySelectedPanel(), Optional.of(1));

        clickMenu("Panels", "Create (Left)");
        assertEquals(panelControl.getPanelCount(), 3);
        assertEquals(panelControl.getCurrentlySelectedPanel(), Optional.of(0));

        clickMenu("Panels", "Close");
        clickMenu("Panels", "Close");
    }

    private void customizedPanelMenuItemTest(String panelName, String panelFilter){
        PlatformEx.waitOnFxThread();
        clickMenu("Panels", "Auto-create", panelName);

        assertEquals(panelControl.getPanelCount(), 2);
        assertEquals(panelControl.getCurrentlySelectedPanel(), Optional.of(1));

        PanelInfo panelInfo = panelControl.getCurrentPanelInfos().get(1);
        assertEquals(panelInfo.getPanelFilter(), panelFilter);
        assertEquals(panelInfo.getPanelName(), panelName);
        clickMenu("Panels", "Close");
    }

}
