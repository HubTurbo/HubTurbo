package guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;

import prefs.Preferences;
import ui.UI;
import ui.components.PanelNameTextField;
import util.events.ShowRenamePanelEvent;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PanelRenameTest extends UITest {

    public static final int EVENT_DELAY = 500;

    @Override
    public void launchApp() {
        FXTestUtils.launchApp(TestUI.class, "--testconfig=true", "--bypasslogin=true");
    }

    @Test
    public void basicPanelInfoTest() {
        
        // Test for saving panel information with corner cases
        
        // maximize
        press(KeyCode.CONTROL).press(KeyCode.X).release(KeyCode.X).release(KeyCode.CONTROL);
        sleep(EVENT_DELAY);

        // Testing rename cancel
        UI.events.triggerEvent(new ShowRenamePanelEvent(0));
        push(KeyCode.ESCAPE);
        sleep(EVENT_DELAY);
        
        press(KeyCode.CONTROL).press(KeyCode.P).release(KeyCode.P).release(KeyCode.CONTROL);
        
        // Testing panel names between whitespaces
        UI.events.triggerEvent(new ShowRenamePanelEvent(1));
        type("   Renamed panel  ");
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        
        press(KeyCode.CONTROL).press(KeyCode.P).release(KeyCode.P).release(KeyCode.CONTROL);
        
        // Testing empty panel name
        UI.events.triggerEvent(new ShowRenamePanelEvent(2));
        push(KeyCode.BACK_SPACE);
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        
        press(KeyCode.CONTROL).press(KeyCode.P).release(KeyCode.P).release(KeyCode.CONTROL);

        // Testing typing excessive panel name
        UI.events.triggerEvent(new ShowRenamePanelEvent(3));
        sleep(EVENT_DELAY);
        type("1234567890123456789012345678901234567890");
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        
        press(KeyCode.CONTROL).press(KeyCode.P).release(KeyCode.P).release(KeyCode.CONTROL);

        // Testing pasting excessive panel name
        UI.events.triggerEvent(new ShowRenamePanelEvent(4));
        sleep(EVENT_DELAY);
        PanelNameTextField renameTextField1 = find("#dummy/dummy_col4_renameTextField");
        renameTextField1.setText("1234567890123456789012345678901234567890");
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        
        press(KeyCode.CONTROL).press(KeyCode.P).release(KeyCode.P).release(KeyCode.CONTROL);
        
        // Testing typing more characters when textfield is full
        UI.events.triggerEvent(new ShowRenamePanelEvent(5));
        sleep(EVENT_DELAY);
        PanelNameTextField renameTextField2 = find("#dummy/dummy_col5_renameTextField");
        renameTextField2.setText("123456789012345678901234567890123456");
        renameTextField2.positionCaret(4);
        type("characters that will not be added");
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        
        press(KeyCode.CONTROL).press(KeyCode.P).release(KeyCode.P).release(KeyCode.CONTROL);
        
        // Testing typing excessive characters when textfield is almost full
        UI.events.triggerEvent(new ShowRenamePanelEvent(6));
        sleep(EVENT_DELAY);
        PanelNameTextField renameTextField3 = find("#dummy/dummy_col6_renameTextField");
        renameTextField3.setText("123456789012345678901234567890");
        renameTextField3.positionCaret(20); // between second 0 and 1
        type("1234567890");
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        
        // Quitting to update json
        click("File");
        push(KeyCode.DOWN).push(KeyCode.DOWN).push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        
        Preferences testPref = new Preferences(true);
        
        List<String> openPanelNames = testPref.getPanelNames();
        
        assertEquals(7, openPanelNames.size());

        assertEquals("Panel", openPanelNames.get(0));
        assertEquals("Renamed panel", openPanelNames.get(1));
        assertEquals("Panel", openPanelNames.get(2));
        assertEquals("123456789012345678901234567890123456", openPanelNames.get(3));
        assertEquals("Panel", openPanelNames.get(4));
        assertEquals("123456789012345678901234567890123456", openPanelNames.get(5));
        assertEquals("123456789012345678901234561234567890", openPanelNames.get(6));
    }
}
