package guitests;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;

import prefs.Preferences;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PanelInfoCornerTest extends UITest {

    @Override
    public void launchApp() {
        // isTestMode in UI checks for testconfig too so we don't need to specify --test=true here.
        FXTestUtils.launchApp(TestUI.class, "--testconfig=true");
    }

    @Test
    public void panelsTest() {
        
        TextField repoOwnerField = find("#repoOwnerField");
        doubleClick(repoOwnerField);
        doubleClick(repoOwnerField);
        type("dummy").push(KeyCode.TAB);
        type("dummy").push(KeyCode.TAB);
        type("test").push(KeyCode.TAB);
        type("test");
        click("Sign in");
        sleep(2000);
        
        // Test for saving panel information stored after some corner cases
        
        // maximize
        press(KeyCode.CONTROL).press(KeyCode.X).release(KeyCode.X).release(KeyCode.CONTROL);

        // Corner case: submitting empty name
        // Expected behavior: no change to panel name
        click("RENAME");
        press(KeyCode.BACK_SPACE).press(KeyCode.ENTER);
        
        // Adding normal panels
        press(KeyCode.CONTROL).press(KeyCode.P).release(KeyCode.P).release(KeyCode.CONTROL);
        click("#dummy/dummy_col1_filterTextField");
        type("repo");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("dummy2/dummy2").push(KeyCode.ENTER);
        click("#dummy/dummy_col1_renameButton");
        type("Dummy 2 panel").push(KeyCode.ENTER);
        
        press(KeyCode.CONTROL).press(KeyCode.P).release(KeyCode.P).release(KeyCode.CONTROL);
        click("#dummy/dummy_col2_filterTextField");
        type("is");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("pr").push(KeyCode.ENTER);
        click("#dummy/dummy_col2_renameButton");
        type("PRs").push(KeyCode.ENTER);
        
        // Deleting right panel
        click("#dummy/dummy_col2_closeButton");
        
        press(KeyCode.CONTROL).press(KeyCode.P).release(KeyCode.P).release(KeyCode.CONTROL);
        click("#dummy/dummy_col2_filterTextField");
        type("id");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("4").push(KeyCode.ENTER);
        click("#dummy/dummy_col2_renameButton");
        type("Specific ID").push(KeyCode.ENTER);
        
        // Quitting to update json
        click("File");
        click("Quit");
        
        Preferences testPref = new Preferences(true);
        List<String> openPanels = testPref.getPanelNames();
        List<String> openFilters = testPref.getLastOpenFilters();
        
        assertEquals(3, openPanels.size());
        assertEquals(3, openFilters.size());

        assertEquals("Panel", openPanels.get(0));
        assertEquals("Dummy 2 panel", openPanels.get(1));
        assertEquals("Specific ID", openPanels.get(2));

        assertEquals("", openFilters.get(0));
        assertEquals("repo:dummy2/dummy2", openFilters.get(1));
        assertEquals("id:4", openFilters.get(2));
    }
}
