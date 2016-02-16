package guitests;

import javafx.scene.input.KeyCode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;
import prefs.PanelInfo;
import prefs.Preferences;
import ui.BoardAutoCreator;
import ui.TestController;
import ui.UI;
import ui.issuepanel.PanelControl;
import static guitests.BoardAutoCreatorTest.testSamplePanelInfos;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static ui.BoardAutoCreator.SAMPLE_BOARD;

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
        dummyLogin();
        logout();

        assertEquals(panelControl.getPanelCount(), BoardAutoCreator.getSamplePanelDetails().size());
        assertEquals(panelControl.getCurrentlySelectedPanel(), Optional.of(0));
        assertEquals(panelControl.getNumberOfSavedBoards(), 1);

        assertEquals(testPref.getAllBoardNames().get(0), SAMPLE_BOARD);
        testSamplePanelInfos(panelControl);
    }

    private void dummyLogin(){
        selectAll();
        type("dummy").push(KeyCode.TAB);
        type("dummy").push(KeyCode.TAB);
        type("test").push(KeyCode.TAB);
        type("test");
        click("Sign in");
    }

    private void logout(){
        clickMenu("File", "Logout");
    }

    @After
    public void teardown(){
        clearTestFolder();
    }
}
