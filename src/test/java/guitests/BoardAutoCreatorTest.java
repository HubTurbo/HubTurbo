package guitests;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.loadui.testfx.Assertions.assertNodeExists;
import static org.loadui.testfx.controls.Commons.hasText;

import javafx.scene.input.KeyCode;

import prefs.PanelInfo;
import prefs.Preferences;
import ui.TestController;
import ui.UI;
import ui.issuepanel.PanelControl;
import util.PlatformEx;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BoardAutoCreatorTest extends UITest {
    private UI ui;
    private PanelControl panelControl;
    private Preferences testPref;

    @Before
    public void cleanUpBoards() {
        ui = TestController.getUI();
        panelControl = ui.getPanelControl();
        testPref = UI.prefs;

        List<String> boardNames = testPref.getAllBoards().keySet().stream().collect(Collectors.toList());
        boardNames.stream().forEach(testPref::removeBoard);
    }

    @Test
    public void milestoneBoardAutoCreationTest() {

        assertEquals(panelControl.getNumberOfSavedBoards(), 0);

        click("Boards");
        push(KeyCode.DOWN); // Save
        push(KeyCode.DOWN); // Save As
        push(KeyCode.DOWN); // Open
        push(KeyCode.DOWN); // Delete
        push(KeyCode.DOWN); // Auto-create
        push(KeyCode.RIGHT); // Open Auto-create
        push(KeyCode.ENTER); // Milestones

        PlatformEx.waitOnFxThread();
        assertNodeExists(hasText("Milestones board has been created and loaded.\n\n" +
                "It is saved under the name \"Milestones\"."));
        click("OK");

        assertEquals(panelControl.getNumberOfPanels(), 5);
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

}
