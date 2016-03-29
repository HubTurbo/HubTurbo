package guitests;

import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;

import prefs.Preferences;
import prefs.PanelInfo;
import ui.IdGenerator;
import ui.TestController;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class NoPanelAtQuitTest extends UITest {

    @Override
    public void launchApp() {
        // isTestMode in UI checks for testconfig too so we don't need to specify --test=true here.
        FXTestUtils.launchApp(TestUI.class, "--testconfig=true", "--bypasslogin=true");
    }

    @Test
    public void emptyPanelsInfoTest() {
        String panelCloseButtonId = IdGenerator.getPanelCloseButtonIdReference(0);

        // Test for saving panel information when there are no panels at termination.

        // close open panel
        click(panelCloseButtonId);

        // Quitting to update json
        click("File");
        click("Quit");

        Preferences testPref = TestController.loadTestPreferences();
        List<PanelInfo> lastSessionPanels = testPref.getPanelInfo();

        // Expected result: nothing stored in panelInfo

        assertEquals(0, lastSessionPanels.size());
    }
}
