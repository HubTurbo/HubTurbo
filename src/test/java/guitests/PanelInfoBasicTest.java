package guitests;

import javafx.scene.input.KeyCode;
import javafx.scene.control.Button;

import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;

import prefs.Preferences;
import ui.components.FilterTextField;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PanelInfoBasicTest extends UITest {

    public static final int DIALOG_DELAY = 4000;
    public static final int EVENT_DELAY = 8000;

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
        
        Button renameButton1 = find("#dummy/dummy_col0_renameButton");
        click(renameButton1);
        sleep(DIALOG_DELAY);
        type("Renamed panel");
        sleep(DIALOG_DELAY);
        press(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        
        // Creating panel
        press(KeyCode.CONTROL).press(KeyCode.P).release(KeyCode.P).release(KeyCode.CONTROL);
        sleep(EVENT_DELAY);
        
        FilterTextField filterTextField2 = find("#dummy/dummy_col1_filterTextField");
        click(filterTextField2);
        type("repo");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("dummy2/dummy2");
        sleep(DIALOG_DELAY);
        assertEquals("repo:dummy2/dummy2", filterTextField2.getText());
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        
        Button renameButton2 = find("#dummy/dummy_col1_renameButton");
        click(renameButton2);
        sleep(DIALOG_DELAY);
        type("Dummy 2 panel");
        sleep(DIALOG_DELAY);
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        
        // Creating panel to the left
        press(KeyCode.SHIFT).press(KeyCode.CONTROL).press(KeyCode.P);
        release(KeyCode.P).release(KeyCode.CONTROL).release(KeyCode.SHIFT);
        sleep(EVENT_DELAY);
        
        FilterTextField filterTextField3 = find("#dummy/dummy_col0_filterTextField");
        click(filterTextField3);
        type("is");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("open");
        sleep(DIALOG_DELAY);
        assertEquals("is:open", filterTextField3.getText());
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        
        Button renameButton3 = find("#dummy/dummy_col0_renameButton");
        click(renameButton3);
        sleep(DIALOG_DELAY);
        type("Open issues");
        sleep(DIALOG_DELAY);
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        
        // Quitting to update json
        click("File");
        push(KeyCode.DOWN).push(KeyCode.DOWN).push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        
        Preferences testPref = new Preferences(true);
        
        List<String> openPanels = testPref.getPanelNames();
        List<String> openFilters = testPref.getLastOpenFilters();
        
        assertEquals(3, openPanels.size());
        assertEquals(3, openFilters.size());

        assertEquals("Open issues", openPanels.get(0));
        assertEquals("Renamed panel", openPanels.get(1));
        assertEquals("Dummy 2 panel", openPanels.get(2));

        assertEquals("is:open", openFilters.get(0));
        assertEquals("", openFilters.get(1));
        assertEquals("repo:dummy2/dummy2", openFilters.get(2));
    }
}
