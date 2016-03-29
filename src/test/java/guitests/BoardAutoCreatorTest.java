package guitests;

import static org.junit.Assert.assertEquals;
import static org.loadui.testfx.Assertions.assertNodeExists;
import static org.loadui.testfx.controls.Commons.hasText;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import prefs.PanelInfo;
import prefs.Preferences;
import ui.BoardAutoCreator;
import ui.TestController;
import ui.UI;
import ui.issuepanel.PanelControl;
import util.PlatformEx;

import static ui.BoardAutoCreator.SAMPLE_BOARD;
import static ui.BoardAutoCreator.SAMPLE_BOARD_DIALOG;

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

        assertEquals(0, panelControl.getNumberOfSavedBoards());

        traverseMenu("Boards", "Auto-create", "Milestones");

        PlatformEx.waitOnFxThread();
        assertNodeExists(hasText("Milestones board has been created and loaded.\n\n"
                + "It is saved under the name \"Milestones\"."));
        click("OK");

        assertEquals(5, panelControl.getPanelCount());
        assertEquals(Optional.of(1), panelControl.getCurrentlySelectedPanel());
        assertEquals(1, panelControl.getNumberOfSavedBoards());

        List<PanelInfo> panelInfos = panelControl.getCurrentPanelInfos();

        assertEquals("milestone:curr-1 sort:status", panelInfos.get(0).getPanelFilter());
        assertEquals("milestone:curr sort:status", panelInfos.get(1).getPanelFilter());
        assertEquals("milestone:curr+1 sort:status", panelInfos.get(2).getPanelFilter());
        assertEquals("milestone:curr+2 sort:status", panelInfos.get(3).getPanelFilter());
        assertEquals("milestone:curr+3 sort:status", panelInfos.get(4).getPanelFilter());

        assertEquals("Previous Milestone", panelInfos.get(0).getPanelName());
        assertEquals("Current Milestone", panelInfos.get(1).getPanelName());
        assertEquals("Next Milestone", panelInfos.get(2).getPanelName());
        assertEquals("Next Next Milestone", panelInfos.get(3).getPanelName());
        assertEquals("Next Next Next Milestone", panelInfos.get(4).getPanelName());
    }

    @Test
    public void workAllocationBoardAutoCreationTest() {
        assertEquals(0, panelControl.getNumberOfSavedBoards());

        traverseMenu("Boards", "Auto-create", "Work Allocation");

        PlatformEx.waitOnFxThread();
        assertNodeExists(hasText("Work Allocation board has been created and loaded.\n\n"
                + "It is saved under the name \"Work Allocation\"."));
        click("OK");

        assertEquals(5, panelControl.getPanelCount());
        assertEquals(Optional.of(0), panelControl.getCurrentlySelectedPanel());
        assertEquals(1, panelControl.getNumberOfSavedBoards());

        List<PanelInfo> panelInfos = panelControl.getCurrentPanelInfos();

        assertEquals("assignee:User 1 sort:milestone,status", panelInfos.get(0).getPanelFilter());
        assertEquals("assignee:User 10 sort:milestone,status", panelInfos.get(1).getPanelFilter());
        assertEquals("assignee:User 11 sort:milestone,status", panelInfos.get(2).getPanelFilter());
        assertEquals("assignee:User 12 sort:milestone,status", panelInfos.get(3).getPanelFilter());
        assertEquals("assignee:User 2 sort:milestone,status", panelInfos.get(4).getPanelFilter());

        assertEquals("Work allocated to User 1", panelInfos.get(0).getPanelName());
        assertEquals("Work allocated to User 10", panelInfos.get(1).getPanelName());
        assertEquals("Work allocated to User 11", panelInfos.get(2).getPanelName());
        assertEquals("Work allocated to User 12", panelInfos.get(3).getPanelName());
        assertEquals("Work allocated to User 2", panelInfos.get(4).getPanelName());
    }

    @Test
    public void sampleBoardAutoCreationTest() {
        assertEquals(0, panelControl.getNumberOfSavedBoards());

        traverseMenu("Boards", "Auto-create", SAMPLE_BOARD);

        waitUntilNodeAppears(SAMPLE_BOARD_DIALOG);
        click("OK");
        verifyBoard(panelControl, BoardAutoCreator.getSamplePanelDetails());
    }

    /**
     * Confirms the currently displayed board consists the set of panels specified in panelDetails
     */
    public static void verifyBoard(PanelControl pc, Map<String, String> panelDetails) {
        List<PanelInfo> panelInfos = pc.getCurrentPanelInfos();
        assertEquals(panelDetails.size(), pc.getPanelCount());
        assertEquals(Optional.of(0), pc.getCurrentlySelectedPanel());
        assertEquals(1, pc.getNumberOfSavedBoards());
        int i = 0;
        for (String panelName : panelDetails.keySet()) {
            assertEquals(panelName, panelInfos.get(i).getPanelName());
            assertEquals(BoardAutoCreator.getSamplePanelDetails().get(panelName), panelInfos.get(i).getPanelFilter());
            i++;
        }
    }

}
