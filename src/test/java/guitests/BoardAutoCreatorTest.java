package guitests;

import static org.junit.Assert.assertEquals;
import static org.loadui.testfx.Assertions.assertNodeExists;
import static org.loadui.testfx.controls.Commons.hasText;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import prefs.PanelInfo;
import prefs.Preferences;
import ui.TestController;
import ui.UI;
import ui.issuepanel.PanelControl;
import util.PlatformEx;
import static ui.BoardAutoCreator.SAMPLE_BOARD;
import static ui.BoardAutoCreator.SAMPLE_BOARD_DIALOG;
import static ui.BoardAutoCreator.SAMPLE_PANEL_FILTERS;
import static ui.BoardAutoCreator.SAMPLE_PANEL_NAMES;

public class BoardAutoCreatorTest extends UITest {

    private PanelControl panelControl;

    @Before
    public void cleanUpBoards() {
        UI ui = TestController.getUI();
        panelControl = ui.getPanelControl();
        Preferences testPref = UI.prefs;

        List<String> boardNames = testPref.getAllBoardNames();
        boardNames.stream().forEach(testPref::removeBoard);
    }

    @Test
    public void milestoneBoardAutoCreationTest() {

        assertEquals(panelControl.getNumberOfSavedBoards(), 0);

        clickMenu("Boards", "Auto-create", "Milestones");

        PlatformEx.waitOnFxThread();
        assertNodeExists(hasText("Milestones board has been created and loaded.\n\n" +
                "It is saved under the name \"Milestones\"."));
        click("OK");

        assertEquals(panelControl.getPanelCount(), 5);
        assertEquals(panelControl.getCurrentlySelectedPanel(), Optional.of(1));
        assertEquals(panelControl.getNumberOfSavedBoards(), 1);

        List<PanelInfo> panelInfos = panelControl.getCurrentPanelInfos();

        assertEquals(panelInfos.get(0).getPanelFilter(), "milestone:curr-1 sort:status");
        assertEquals(panelInfos.get(1).getPanelFilter(), "milestone:curr sort:status");
        assertEquals(panelInfos.get(2).getPanelFilter(), "milestone:curr+1 sort:status");
        assertEquals(panelInfos.get(3).getPanelFilter(), "milestone:curr+2 sort:status");
        assertEquals(panelInfos.get(4).getPanelFilter(), "milestone:curr+3 sort:status");

        assertEquals(panelInfos.get(0).getPanelName(), "Previous Milestone");
        assertEquals(panelInfos.get(1).getPanelName(), "Current Milestone");
        assertEquals(panelInfos.get(2).getPanelName(), "Next Milestone");
        assertEquals(panelInfos.get(3).getPanelName(), "Next Next Milestone");
        assertEquals(panelInfos.get(4).getPanelName(), "Next Next Next Milestone");
    }

    @Test
    public void workAllocationBoardAutoCreationTest() {
        assertEquals(panelControl.getNumberOfSavedBoards(), 0);

        clickMenu("Boards", "Auto-create", "Work Allocation");

        PlatformEx.waitOnFxThread();
        assertNodeExists(hasText("Work Allocation board has been created and loaded.\n\n" +
                "It is saved under the name \"Work Allocation\"."));
        click("OK");

        assertEquals(panelControl.getPanelCount(), 5);
        assertEquals(panelControl.getCurrentlySelectedPanel(), Optional.of(0));
        assertEquals(panelControl.getNumberOfSavedBoards(), 1);

        List<PanelInfo> panelInfos = panelControl.getCurrentPanelInfos();

        assertEquals(panelInfos.get(0).getPanelFilter(), "assignee:User 1 sort:milestone,status");
        assertEquals(panelInfos.get(1).getPanelFilter(), "assignee:User 10 sort:milestone,status");
        assertEquals(panelInfos.get(2).getPanelFilter(), "assignee:User 2 sort:milestone,status");
        assertEquals(panelInfos.get(3).getPanelFilter(), "assignee:User 3 sort:milestone,status");
        assertEquals(panelInfos.get(4).getPanelFilter(), "assignee:User 4 sort:milestone,status");

        assertEquals(panelInfos.get(0).getPanelName(), "Work allocated to User 1");
        assertEquals(panelInfos.get(1).getPanelName(), "Work allocated to User 10");
        assertEquals(panelInfos.get(2).getPanelName(), "Work allocated to User 2");
        assertEquals(panelInfos.get(3).getPanelName(), "Work allocated to User 3");
        assertEquals(panelInfos.get(4).getPanelName(), "Work allocated to User 4");
    }

    @Test
    public void sampleBoardAutoCreationTest() {
        assertEquals(panelControl.getNumberOfSavedBoards(), 0);

        clickMenu("Boards", "Auto-create", SAMPLE_BOARD);

        waitUntilNodeAppears(SAMPLE_BOARD_DIALOG);
        click("OK");

        assertEquals(panelControl.getPanelCount(), SAMPLE_PANEL_NAMES.size());
        assertEquals(panelControl.getCurrentlySelectedPanel(), Optional.of(0));
        assertEquals(panelControl.getNumberOfSavedBoards(), 1);

        List<PanelInfo> panelInfos = panelControl.getCurrentPanelInfos();
        for (int i = 0; i < SAMPLE_PANEL_NAMES.size(); i++){
            assertEquals(panelInfos.get(i).getPanelFilter(), SAMPLE_PANEL_FILTERS.get(i));
            assertEquals(panelInfos.get(i).getPanelName(), SAMPLE_PANEL_NAMES.get(i));
        }
    }

}
