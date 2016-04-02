package guitests;

import org.junit.Test;
import org.testfx.api.FxToolkit;

import prefs.Preferences;
import prefs.PanelInfo;
import ui.IdGenerator;
import ui.TestController;

import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;

public class NoPanelAtQuitTest extends UITest {

    @Override
    public void setup() throws TimeoutException {
        // isTestMode in UI checks for testconfig too so we don't need to specify --test=true here.
        FxToolkit.setupApplication(TestUI.class, "--testconfig=true", "--bypasslogin=true");
    }

    @Test
    public void emptyPanelsInfoTest() {
        String panelCloseButtonId = IdGenerator.getPanelCloseButtonIdReference(0);

        // Test for saving panel information when there are no panels at termination.

        // close open panel
        waitBeforeClick(panelCloseButtonId);

        // Quitting to update json
        traverseMenu("File", "Quit");
        
        Preferences testPref = TestController.loadTestPreferences();
        List<PanelInfo> lastSessionPanels = testPref.getPanelInfo();

        // Expected result: nothing stored in panelInfo

        assertEquals(0, lastSessionPanels.size());
    }
}
