package guitests;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.junit.After;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;
import prefs.Preferences;
import ui.RepositorySelector;
import ui.UI;
import ui.issuepanel.IssuePanel;
import util.events.UILogicRefreshEvent;
import util.events.UpdateDummyRepoEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RangeTest extends UITest {

    private static final int EVENT_DELAY = 1000;

    @Override
    public void launchApp() {
        FXTestUtils.launchApp(TestUI.class, "--test=true", "--bypasslogin=true");
    }

    @Test
    public void numberRangeTest() {
        ((TextField) find("#dummy/dummy_col0_filterTextField")).setText("id:>5");
        click("#dummy/dummy_col0_filterTextField");
        press(KeyCode.ENTER).release(KeyCode.ENTER);
        assertEquals(5, ((IssuePanel) find("#dummy/dummy_col0")).getIssueCount());
    }

    @Test
    public void dateRangeTest() {
        ((TextField) find("#dummy/dummy_col0_filterTextField")).setText("created:>2002-12-31");
        click("#dummy/dummy_col0_filterTextField");
        press(KeyCode.ENTER).release(KeyCode.ENTER);
        assertEquals(6, ((IssuePanel) find("#dummy/dummy_col0")).getIssueCount());
    }
}
