package guitests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;
import org.loadui.testfx.utils.TestUtils;
import ui.BoardAutoCreator;
import ui.TestController;
import ui.issuepanel.PanelControl;
import util.PlatformEx;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import static guitests.BoardAutoCreatorTest.verifyBoard;

import static org.junit.Assert.assertEquals;
import static ui.BoardAutoCreator.SAMPLE_BOARD;

public class StartupBoardLauncherTest extends UITest {

    private PanelControl panelControl;

    @Override
    public void launchApp() {
        FXTestUtils.launchApp(TestUI.class, "--startupboard=true");
    }

    @Before
    public void init() {
        panelControl = TestController.getUI().getPanelControl();
    }

    @Test
    public void boardCreation_firstTimeUser_sampleBoardCreated() {
        login("dummy", "dummy", "test", "test");
        TestUtils.awaitCondition(() -> BoardAutoCreator.getSamplePanelDetails().size() == countPanelsShown());

        //Ensures that only 1 board was created and it was the sample board
        List<String> boardNames = panelControl.getAllBoardNames();
        assertEquals(Arrays.asList(new String[] { SAMPLE_BOARD }), boardNames);

        //Verifies the panel details of the sample board created.
        verifyBoard(panelControl, BoardAutoCreator.getSamplePanelDetails());
    }

    private int countPanelsShown() throws InterruptedException, ExecutionException {
        FutureTask<Integer> countPanels = new FutureTask<>(panelControl::getPanelCount);
        PlatformEx.runAndWait(countPanels);
        return countPanels.get();
    }

    @After
    public void teardown() {
        clearTestFolder();
    }
}
