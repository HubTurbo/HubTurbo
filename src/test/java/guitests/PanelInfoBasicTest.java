package guitests;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;

import prefs.Preferences;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PanelInfoBasicTest extends UITest {

    @Override
    public void launchApp() {
        // isTestMode in UI checks for testconfig too so we don't need to specify --test=true here.
        FXTestUtils.launchApp(TestUI.class, "--testconfig=true");
    }

    @Test
    public void basicPanelInfoTest() {
        
        TextField repoOwnerField = find("#repoOwnerField");
        doubleClick(repoOwnerField);
        doubleClick(repoOwnerField);
        type("dummy").push(KeyCode.TAB);
        type("dummy").push(KeyCode.TAB);
        type("test").push(KeyCode.TAB);
        type("test");
        click("Sign in");
        sleep(2000);
        
        // Test for basic functions of saving panel information.
        // Only involves panel additions.
        
        // maximize
        press(KeyCode.CONTROL).press(KeyCode.X).release(KeyCode.X).release(KeyCode.CONTROL);
        
        click("RENAME");
        type("new panel").press(KeyCode.ENTER);
        
        press(KeyCode.CONTROL).press(KeyCode.P).release(KeyCode.P).release(KeyCode.CONTROL);
        click("#dummy/dummy_col1_filterTextField");
        type("repo");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("dummy2/dummy2").push(KeyCode.ENTER);
        click("#dummy/dummy_col1_renameButton");
        type("Dummy 2 repo").push(KeyCode.ENTER);
        
        // Creating panel to the left
        press(KeyCode.SHIFT).press(KeyCode.CONTROL).press(KeyCode.P);
        release(KeyCode.P).release(KeyCode.CONTROL).release(KeyCode.SHIFT);
        click("#dummy/dummy_col0_filterTextField");
        type("is");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("open").push(KeyCode.ENTER);
        click("#dummy/dummy_col0_renameButton");
        type("Open issues").push(KeyCode.ENTER);
        
        // Quitting to update json
        click("File");
        click("Quit");
        
        Preferences testPref = new Preferences(true);
        List<String> openPanels = testPref.getPanelNames();
        List<String> openFilters = testPref.getLastOpenFilters();
        
        assertEquals(3, openPanels.size());
        assertEquals(3, openFilters.size());

        assertEquals("Open issues", openPanels.get(0));
        assertEquals("new panel", openPanels.get(1));
        assertEquals("Dummy 2 repo", openPanels.get(2));

        assertEquals("is:open", openFilters.get(0));
        assertEquals("", openFilters.get(1));
        assertEquals("repo:dummy2/dummy2", openFilters.get(2));
    }
}
