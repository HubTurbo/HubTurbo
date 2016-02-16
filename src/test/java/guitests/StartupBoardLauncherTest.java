package guitests;

import org.junit.Before;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;
import prefs.Preferences;
import ui.BoardAutoCreator;
import ui.TestController;
import ui.UI;
import ui.issuepanel.PanelControl;

import static guitests.BoardAutoCreatorTest.verifyBoard;

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
    public void boardCreation_firstTimeUser_sampleBoardCreated(){
        login("dummy", "dummy", "test", "test");
        // Workaround since we are unable to synchronize board creation on Travis post login through the dialog box.
        logout();

        //Ensures that only 1 board was created and it was the sample board
        assertEquals(testPref.getAllBoardNames().size(), 1);
        assertEquals(testPref.getAllBoardNames().get(0), SAMPLE_BOARD);

        //Verifies the panel details of the sample board created.
        verifyBoard(panelControl, BoardAutoCreator.getSamplePanelDetails());
    }
}
