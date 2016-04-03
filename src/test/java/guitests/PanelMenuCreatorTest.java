package guitests;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import prefs.PanelInfo;
import ui.*;
import ui.issuepanel.PanelControl;
import util.PlatformEx;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

public class PanelMenuCreatorTest extends UITest {

    private PanelControl panelControl;
    private UI ui;

    @Before
    public void setup() {
        ui = TestController.getUI();
        panelControl = ui.getPanelControl();
    }

    @Test
    public void customPanelMenuItemTest() throws NoSuchFieldException, IllegalAccessException {
        Field panelMenuCreatorField = MenuControl.class.getDeclaredField("panelMenuCreator");
        panelMenuCreatorField.setAccessible(true);
        PanelMenuCreator value = (PanelMenuCreator) panelMenuCreatorField.get(ui.getMenuControl());
        for (Map.Entry<String, String> entry :
                value.generateCustomizedPanelDetails(ui.prefs.getLastLoginUsername()).entrySet()) {
            customizedPanelMenuItemTest(entry.getKey(), entry.getValue());
        }
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
