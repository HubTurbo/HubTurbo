package guitests;

import org.junit.Test;

import org.loadui.testfx.utils.FXTestUtils;
import prefs.ConfigFileHandler;
import prefs.GlobalConfig;
import prefs.PanelInfo;
import prefs.Preferences;
import ui.issuepanel.PanelControl;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PanelFocusTest extends UITest {

    @Override
    public void launchApp() {
        FXTestUtils.launchApp(TestUI.class, "--testconfig=true", "--bypasslogin=true");
    }

    @Override
    public void setupMethod() {
        ConfigFileHandler configFileHandler =
                new ConfigFileHandler(Preferences.DIRECTORY, Preferences.TEST_CONFIG_FILE);
        GlobalConfig globalConfig = new GlobalConfig();

        PanelInfo test1 = new PanelInfo();
        PanelInfo test2 = new PanelInfo();
        PanelInfo test3 = new PanelInfo();
        List<PanelInfo> panels = new ArrayList<>();
        panels.add(test1);
        panels.add(test2);
        panels.add(test3);

        globalConfig.setPanelInfo(panels);
        configFileHandler.saveGlobalConfig(globalConfig);
    }

    /**
     * Only doing test for multiple panel at the start (not testing cases of
     * 1 recent panel or 0 recent panel) since it is only possible to launch
     * the application once for one test.
     * Having tests with multiple start ups will require each start up case
     * to be in its own test file.
     */
    @Test
    public void panelFocusOnFirstPanelAtStartupTest() {
        PanelControl panelControl = (PanelControl) find("#dummy/dummy_col0").getParent();

        assertEquals(3, panelControl.getNumberOfPanels());
        assertEquals(0, (int) panelControl.getCurrentlySelectedPanel().get());
    }
}
