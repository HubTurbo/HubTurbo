package guitests;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import ui.issuepanel.IssuePanel;

import static org.junit.Assert.assertEquals;

public class RangeTest extends UITest {

    private static final int EVENT_DELAY = 500;

    @Test
    public void numberRangeTest() {
        ((TextField) find("#dummy/dummy_col0_filterTextField")).setText("id:>5");
        click("#dummy/dummy_col0_filterTextField");
        press(KeyCode.ENTER).release(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        assertEquals(5, ((IssuePanel) find("#dummy/dummy_col0")).getIssueCount());
    }

    @Test
    public void dateRangeTest() {
        ((TextField) find("#dummy/dummy_col0_filterTextField")).setText("created:>2002-12-31");
        click("#dummy/dummy_col0_filterTextField");
        press(KeyCode.ENTER).release(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        assertEquals(6, ((IssuePanel) find("#dummy/dummy_col0")).getIssueCount());
    }
}
