package guitests;

import backend.stub.DummyRepoState;
import javafx.scene.input.KeyCode;
import ui.listpanel.ListPanel;

import org.junit.Test;
import org.loadui.testfx.GuiTest;

import static org.junit.Assert.assertEquals;

public class RangeTest extends UITest {

    private static final int EVENT_DELAY = 1000;

    @Test
    public void numberRangeTest() {
        getFilterTextFieldAtPanel(0).setText("id:>5");
        clickFilterTextFieldAtPanel(0);
        press(KeyCode.ENTER).release(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        assertEquals(DummyRepoState.NO_OF_DUMMY_ISSUES - 5, getPanel(0).getIssuesCount());
        assertEquals(DummyRepoState.NO_OF_DUMMY_ISSUES - 5, ((ListPanel) GuiTest.find("#dummy/dummy_col0")).getIssuesCount());
    }

    @Test
    public void dateRangeTest() {
        getFilterTextFieldAtPanel(0).setText("created:>2002-12-31");
        clickFilterTextFieldAtPanel(0);
        press(KeyCode.ENTER).release(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        assertEquals(8, getPanel(0).getIssuesCount());
    }
}
