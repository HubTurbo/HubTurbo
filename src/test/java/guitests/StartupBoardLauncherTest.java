package guitests;

import javafx.scene.input.KeyCode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;
import prefs.PanelInfo;
import prefs.Preferences;
import ui.TestController;
import ui.UI;
import ui.issuepanel.PanelControl;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static ui.BoardAutoCreator.SAMPLE_BOARD;
import static ui.BoardAutoCreator.SAMPLE_PANEL_FILTERS;
import static ui.BoardAutoCreator.SAMPLE_PANEL_NAMES;

public class StartupBoardLauncherTest extends UITest{

    private PanelControl panelControl;
    private Preferences testPref;

    @Override
    public void launchApp(){
        FXTestUtils.launchApp(TestUI.class, "--startupboard=true");
    }

    @Before
    public void init(){
        UI ui = TestController.getUI();
        panelControl = ui.getPanelControl();
        testPref = UI.prefs;
    }

    @Test
    public void testSampleBoardOnLaunch(){
        selectAll();
        type("dummy").push(KeyCode.TAB);
        type("dummy").push(KeyCode.TAB);
        type("test").push(KeyCode.TAB);
        type("test");
        click("Sign in");
        clickMenu("File", "Logout");

        assertEquals(panelControl.getPanelCount(), SAMPLE_PANEL_NAMES.size());
        assertEquals(panelControl.getCurrentlySelectedPanel(), Optional.of(0));
        assertEquals(panelControl.getNumberOfSavedBoards(), 1);

        assertEquals(testPref.getAllBoardNames().get(0), SAMPLE_BOARD);
        List<PanelInfo> panelInfos = panelControl.getCurrentPanelInfos();
        for (int i = 0; i < SAMPLE_PANEL_NAMES.size(); i++){
            assertEquals(panelInfos.get(i).getPanelFilter(), SAMPLE_PANEL_FILTERS.get(i));
            assertEquals(panelInfos.get(i).getPanelName(), SAMPLE_PANEL_NAMES.get(i));
        }
    }

    @After
    public void teardown(){
        clearTestFolder();
    }
}
