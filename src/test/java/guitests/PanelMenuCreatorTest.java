package guitests;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import prefs.PanelInfo;
import ui.TestController;
import ui.UI;
import ui.issuepanel.PanelControl;
import java.util.Optional;

public class PanelMenuCreatorTest extends UITest{
    private PanelControl panelControl;
    private UI ui;

    @Before
    public void setup() {
        ui = TestController.getUI();
        panelControl = ui.getPanelControl();
    }

    @Test
    public void assigneePanelTest(){
        clickMenu("Panels", "Self-assigned issues");

        assertEquals(panelControl.getPanelCount(), 2);
        assertEquals(panelControl.getCurrentlySelectedPanel(), Optional.of(1));

        PanelInfo panelInfo = panelControl.getCurrentPanelInfos().get(1);
        assertEquals(panelInfo.getPanelFilter(), "is:open ((is:issue assignee:me) OR (is:pr author:me))");
        assertEquals(panelInfo.getPanelName(), "Open issues and PR's");
        clickMenu("Panels", "Close");
    }

    @Test
    public void milestonePanelTest(){
        clickMenu("Panels", "Current Milestone");

        assertEquals(panelControl.getPanelCount(), 2);
        assertEquals(panelControl.getCurrentlySelectedPanel(), Optional.of(1));

        PanelInfo panelInfo = panelControl.getCurrentPanelInfos().get(1);
        assertEquals(panelInfo.getPanelFilter(), "milestone:curr sort:status");
        assertEquals(panelInfo.getPanelName(), "Current Milestone");
        clickMenu("Panels", "Close");
    }

    @Test
    public void recentlyUpdatedPanelTest(){
        clickMenu("Panels", "Recently Updated issues");

        assertEquals(panelControl.getPanelCount(), 2);
        assertEquals(panelControl.getCurrentlySelectedPanel(), Optional.of(1));

        PanelInfo panelInfo = panelControl.getCurrentPanelInfos().get(1);
        assertEquals(panelInfo.getPanelFilter(), "assignee:me updated:<48");
        assertEquals(panelInfo.getPanelName(), "Recently Updated issues");
        clickMenu("Panels", "Close");
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

}
