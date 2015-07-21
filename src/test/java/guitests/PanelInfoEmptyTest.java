package guitests;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;

import prefs.Preferences;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PanelInfoEmptyTest extends UITest {

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
        
        // Test for saving panel information when there are no panels at termination.
        
        // close open panel
        click("#dummy/dummy_col0_closeButton");
        
        // Quitting to update json
        click("File");
        click("Quit");
        
        Preferences testPref = new Preferences(true);
        List<String> openPanels = testPref.getPanelNames();
        List<String> openFilters = testPref.getLastOpenFilters();
        
        // Expected result: nothing stored in panelInfo
        
        assertEquals(0, openPanels.size());
        assertEquals(0, openFilters.size());
    }
}