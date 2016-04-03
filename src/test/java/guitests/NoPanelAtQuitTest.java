package guitests;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.testfx.api.FxToolkit;

import prefs.PanelInfo;
import prefs.Preferences;
import ui.IdGenerator;
import ui.TestController;

public class NoPanelAtQuitTest extends UITest {

    private static final int EVENT_DELAY = 1000;

    @Override
    public void setup() throws TimeoutException {
        // isTestMode in UI checks for testconfig too so we don't need to specify --test=true here.
        FxToolkit.setupApplication(TestUI.class, "--testconfig=true", "--bypasslogin=true");
    }

    @Test
    public void emptyPanelsInfoTest() {
        sleep(EVENT_DELAY);
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
