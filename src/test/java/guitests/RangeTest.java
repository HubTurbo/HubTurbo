package guitests;

import backend.stub.DummyRepoState;
import javafx.scene.input.KeyCode;
import org.junit.Test;
<<<<<<< HEAD
=======
import org.loadui.testfx.GuiTest;

import ui.listpanel.ListPanel;
>>>>>>> Modifies all tests to use new UITest

import static org.junit.Assert.assertEquals;

public class RangeTest extends UITest {

    private static final int EVENT_DELAY = 1000;

    @Test
    public void numberRangeTest() {
<<<<<<< HEAD
        getFilterTextFieldAtPanel(0).setText("id:>5");
        clickFilterTextFieldAtPanel(0);
        press(KeyCode.ENTER).release(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        assertEquals(DummyRepoState.NO_OF_DUMMY_ISSUES - 5, getPanel(0).getIssuesCount());
=======
        ((TextField) GuiTest.find("#dummy/dummy_col0_filterTextField")).setText("id:>5");
        clickOn("#dummy/dummy_col0_filterTextField");
        press(KeyCode.ENTER).release(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        assertEquals(DummyRepoState.NO_OF_DUMMY_ISSUES - 5, ((ListPanel) GuiTest.find("#dummy/dummy_col0")).getIssuesCount());
>>>>>>> Modifies all tests to use new UITest
    }

    @Test
    public void dateRangeTest() {
<<<<<<< HEAD
        getFilterTextFieldAtPanel(0).setText("created:>2002-12-31");
        clickFilterTextFieldAtPanel(0);
        press(KeyCode.ENTER).release(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        assertEquals(8, getPanel(0).getIssuesCount());
=======
        ((TextField) GuiTest.find("#dummy/dummy_col0_filterTextField")).setText("created:>2002-12-31");
        clickOn("#dummy/dummy_col0_filterTextField");
        press(KeyCode.ENTER).release(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        assertEquals(8, ((ListPanel) GuiTest.find("#dummy/dummy_col0")).getIssuesCount());
>>>>>>> Modifies all tests to use new UITest
    }
}
