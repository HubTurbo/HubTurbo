package guitests;

import javafx.scene.input.KeyCode;
import javafx.scene.control.TextField;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;

import prefs.Preferences;
import ui.components.FilterTextField;

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

        doubleClick("#dummy/dummy_col0_nameText");
        type("Renamed panel");
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

        doubleClick("#dummy/dummy_col1_nameText");
        type("Dummy 2 panel");
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

        doubleClick("#dummy/dummy_col0_nameText");
        type("Open issues");
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
