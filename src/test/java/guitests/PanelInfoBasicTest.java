package guitests;

import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;

import prefs.Preferences;
import ui.components.FilterTextField;
import ui.UI;
import ui.issuepanel.FilterPanel;
import util.events.ShowRenamePanelEvent;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PanelInfoBasicTest extends UITest {

    public static final int EVENT_DELAY = 2000;

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
        
        FilterPanel panel1 = find("#dummy/dummy_col0");
        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowRenamePanelEvent(panel1));
        type("Renamed panel");
        click("OK");
        sleep(EVENT_DELAY);
        
        // Creating panel
        press(KeyCode.CONTROL).press(KeyCode.P).release(KeyCode.P).release(KeyCode.CONTROL);
        
        FilterTextField filterTextField2 = find("#dummy/dummy_col1_filterTextField");
        click(filterTextField2);
        type("repo");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("dummy2/dummy2");
        assertEquals("repo:dummy2/dummy2", filterTextField2.getText());
        
        FilterPanel panel2 = find("#dummy/dummy_col1");
        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowRenamePanelEvent(panel2));
        type("Dummy 2 panel");
        click("OK");
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
        sleep(EVENT_DELAY);
        
        FilterPanel panel3 = find("#dummy/dummy_col0");
        Platform.runLater(stage::hide);
        UI.events.triggerEvent(new ShowRenamePanelEvent(panel3));
        type("Open issues");
        click("OK");
        
        // Quitting to update json
        click("File");
        push(KeyCode.DOWN).push(KeyCode.DOWN).push(KeyCode.ENTER);
        
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
