package guitests;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import prefs.PanelInfo;
import ui.*;
import ui.issuepanel.PanelControl;
import util.PlatformEx;
import java.util.Map.Entry;

import java.lang.reflect.Field;
import java.util.Optional;

public class PanelMenuCreatorTest extends UITest {

    private PanelControl panelControl;
    private UI ui;

    @Before
    public void setupUIComponent() {
        ui = TestController.getUI();
        panelControl = ui.getPanelControl();
    }

    @Test
    public void autoCreatePanels_createCustomPanelsFromMenu_panelsCreatedWithAppropriatePanelNameAndFilter()
            throws NoSuchFieldException, IllegalAccessException {
        PanelMenuCreator value = (PanelMenuCreator) getPanelMenuCreatorField().get(ui.getMenuControl());
        for (Entry<String, String> entry :
                value.generatePanelDetails(ui.prefs.getLastLoginUsername()).entrySet()) {
            customizedPanelMenuItemTest(entry.getKey(), entry.getValue());
        }
    }

    private Field getPanelMenuCreatorField() throws NoSuchFieldException, IllegalAccessException {
        Field panelMenuCreatorField = MenuControl.class.getDeclaredField("panelMenuCreator");
        panelMenuCreatorField.setAccessible(true);
        return panelMenuCreatorField;
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
