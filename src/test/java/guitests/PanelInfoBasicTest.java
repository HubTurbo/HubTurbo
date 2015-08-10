package guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;

import prefs.Preferences;
import ui.UI;
import ui.components.FilterTextField;
import ui.components.PanelNameTextField;
import util.events.ShowRenameTextFieldEvent;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PanelInfoBasicTest extends UITest {

    public static final int EVENT_DELAY = 500;

    @Override
    public void launchApp() {
        FXTestUtils.launchApp(TestUI.class, "--testconfig=true", "--bypasslogin=true");
    }

    @Test
    public void basicPanelInfoTest() {
        
        // Test for basic functions of saving panel information.
        // Only involves panel additions.
        
        // maximize
        press(KeyCode.CONTROL).press(KeyCode.X).release(KeyCode.X).release(KeyCode.CONTROL);
        sleep(EVENT_DELAY);

        UI.events.triggerEvent(new ShowRenameTextFieldEvent(0));
        type("Renamed panel");
        PanelNameTextField renameTextField1 = find("#dummy/dummy_col0_renameTextField");
        assertEquals("Renamed panel", renameTextField1.getText());
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        
        // Creating panel
        press(KeyCode.CONTROL).press(KeyCode.P).release(KeyCode.P).release(KeyCode.CONTROL);
        
        FilterTextField filterTextField2 = find("#dummy/dummy_col1_filterTextField");
        click(filterTextField2);
        type("repo");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("dummy2/dummy2");
        assertEquals("repo:dummy2/dummy2", filterTextField2.getText());

        UI.events.triggerEvent(new ShowRenameTextFieldEvent(1));
        type("Dummy 2 panel");
        PanelNameTextField renameTextField2 = find("#dummy/dummy_col1_renameTextField");
        assertEquals("Dummy 2 panel", renameTextField2.getText());
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        
        // Creating panel to the left
        press(KeyCode.SHIFT).press(KeyCode.CONTROL).press(KeyCode.P);
        release(KeyCode.P).release(KeyCode.CONTROL).release(KeyCode.SHIFT);
        
        FilterTextField filterTextField3 = find("#dummy/dummy_col0_filterTextField");
        click(filterTextField3);
        type("is");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("open");
        assertEquals("is:open", filterTextField3.getText());
        push(KeyCode.ENTER);

        UI.events.triggerEvent(new ShowRenameTextFieldEvent(0));
        type("Open issues");
        PanelNameTextField renameTextField3 = find("#dummy/dummy_col0_renameTextField");
        assertEquals("Open issues", renameTextField3.getText());
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        
        // Quitting to update json
        click("File");
        push(KeyCode.DOWN).push(KeyCode.DOWN).push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        
        Preferences testPref = new Preferences(true);
        
        List<String> openPanelNames = testPref.getPanelNames();
        List<String> openPanelFilters = testPref.getLastOpenFilters();
        
        assertEquals(3, openPanelNames.size());
        assertEquals(3, openPanelFilters.size());

        assertEquals("is:open", openPanelFilters.get(0));
        assertEquals("", openPanelFilters.get(1));
        assertEquals("repo:dummy2/dummy2", openPanelFilters.get(2));

        assertEquals("Open issues", openPanelNames.get(0));
        assertEquals("Renamed panel", openPanelNames.get(1));
        assertEquals("Dummy 2 panel", openPanelNames.get(2));
    }
}
