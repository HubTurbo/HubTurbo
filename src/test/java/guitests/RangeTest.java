package guitests;

import backend.stub.DummyRepoState;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import ui.IdGenerator;
import ui.listpanel.ListPanel;

import static org.junit.Assert.assertEquals;

public class RangeTest extends UITest {

    private static final int EVENT_DELAY = 1000;
    private static final String filterTextFieldId = IdGenerator.getPanelFilterTextFieldIdForTest("dummy/dummy", 0);
    private static final String panelId = IdGenerator.getPanelIdForTest("dummy/dummy", 0);

    @Test
    public void numberRangeTest() {
        ((TextField) find(filterTextFieldId)).setText("id:>5");
        click(filterTextFieldId);
        press(KeyCode.ENTER).release(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        assertEquals(DummyRepoState.NO_OF_DUMMY_ISSUES - 5, ((ListPanel) find(panelId)).getIssuesCount());
    }

    @Test
    public void dateRangeTest() {
        ((TextField) find(filterTextFieldId)).setText("created:>2002-12-31");
        click(filterTextFieldId);
        press(KeyCode.ENTER).release(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        assertEquals(8, ((ListPanel) find(panelId)).getIssuesCount());
    }
}
