package guitests;

import java.util.Random;

import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;

import org.junit.Test;
import org.loadui.testfx.exceptions.NoNodesFoundException;
import org.loadui.testfx.utils.FXTestUtils;
import org.apache.commons.lang3.RandomStringUtils;

import ui.TestController;
import ui.UI;
import ui.issuepanel.FilterPanel;
import ui.issuepanel.PanelControl;
import ui.components.PanelNameTextField;
import util.PlatformEx;
import util.events.ShowRenamePanelEvent;

import static org.junit.Assert.assertEquals;
import static ui.components.KeyboardShortcuts.CREATE_RIGHT_PANEL;
import static ui.components.KeyboardShortcuts.MAXIMIZE_WINDOW;

public class PanelRenameTest extends UITest {

    public static final int EVENT_DELAY = 500;
    public static final int PANEL_MAX_NAME_LENGTH = 36;

    @Override
    public void launchApp() {
        FXTestUtils.launchApp(TestUI.class, "--bypasslogin=true");
    }

    @Test
    public void panelRenameTest() {
        
        Random rand = new Random();
        UI ui = TestController.getUI();
        PanelControl panels = ui.getPanelControl();
        
        // Test for saving panel name
        
        press(MAXIMIZE_WINDOW);
        sleep(EVENT_DELAY);

        // Testing case where rename is canceled with ESC
        // Expected: change not reflected
        PlatformEx.runAndWait(() -> UI.events.triggerEvent(new ShowRenamePanelEvent(0)));
        sleep(EVENT_DELAY);
        type("Renamed panel");
        push(KeyCode.ESCAPE);
        FilterPanel panel0 = (FilterPanel) panels.getPanel(0);
        Text panelNameText0 = panel0.getNameText();
        assertEquals("Panel", panelNameText0.getText());
        sleep(EVENT_DELAY);
        
        press(CREATE_RIGHT_PANEL);
        
        // Testing case where a name with whitespaces at either end is submitted
        // Expected: new name accepted with whitespaces removed
        PlatformEx.runAndWait(() -> UI.events.triggerEvent(new ShowRenamePanelEvent(1)));
        sleep(EVENT_DELAY);
        type("   Renamed panel  ");
        push(KeyCode.ENTER);
        FilterPanel panel1 = (FilterPanel) panels.getPanel(1);
        Text panelNameText1 = panel1.getNameText();
        assertEquals("Renamed panel", panelNameText1.getText());
        sleep(EVENT_DELAY);

        press(CREATE_RIGHT_PANEL);

        // Testing case where empty name is submitted
        // Expected: new name not accepted
        PlatformEx.runAndWait(() -> UI.events.triggerEvent(new ShowRenamePanelEvent(2)));
        sleep(EVENT_DELAY);
        push(KeyCode.BACK_SPACE);
        push(KeyCode.ENTER);
        FilterPanel panel2 = (FilterPanel) panels.getPanel(2);
        Text panelNameText2 = panel2.getNameText();
        assertEquals("Panel", panelNameText2.getText());
        sleep(EVENT_DELAY);

        // Testing whether the close button appears once rename box is opened.
        // Expected: Close button should not appear once rename box is opened and while edits are being made.
        //           It should appear once the rename box is closed and the edits are done.
        press(CREATE_RIGHT_PANEL);
        boolean isPresentBeforeEdit = exists("#dummy/dummy_col3_closeButton");
        PlatformEx.runAndWait(() -> UI.events.triggerEvent(new ShowRenamePanelEvent(3)));
        PlatformEx.waitOnFxThread();
        boolean isPresentDuringEdit = true; //stub value, this should change to false.
        try {
            exists("#dummy/dummy_col3_closeButton");
        } catch (NoNodesFoundException e){
            isPresentDuringEdit = false;
        }

        String randomName3 = RandomStringUtils.randomAlphanumeric(PANEL_MAX_NAME_LENGTH - 1);
        PanelNameTextField renameTextField3 = find("#dummy/dummy_col3_renameTextField");
        renameTextField3.setText(randomName3);
        push(KeyCode.ENTER);
        boolean isPresentAfterEdit = exists("#dummy/dummy_col3_closeButton");
        Text panelNameText3 = find("#dummy/dummy_col3_nameText");
        assertEquals(true, isPresentBeforeEdit);
        assertEquals(false, isPresentDuringEdit);
        assertEquals(true, isPresentAfterEdit);
        assertEquals(randomName3, panelNameText3.getText());
        PlatformEx.waitOnFxThread();
        
        // Quitting to update json
        click("File");
        push(KeyCode.DOWN).push(KeyCode.DOWN).push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
    }
}
