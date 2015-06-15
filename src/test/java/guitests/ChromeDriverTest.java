package guitests;

import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;

import javafx.scene.input.KeyCode;
import ui.UI;
import util.events.IssueCreatedEvent;
import util.events.IssueSelectedEvent;
import util.events.LabelCreatedEvent;
import util.events.MilestoneCreatedEvent;

public class ChromeDriverTest extends UITest {

    private final int EVENT_DELAY = 100;

    @Override
    public void launchApp() {
        FXTestUtils.launchApp(
                TestUI.class, "--test=true", "--bypasslogin=true", "--testchromedriver=true");
    }

    // TODO test that events have been triggered
    @Test
    public void chromeDriverStubTest() {
        UI.events.triggerEvent(new IssueSelectedEvent("dummy/dummy", 1, 0, false));
        sleep(EVENT_DELAY);
        UI.events.triggerEvent(new IssueCreatedEvent());
        sleep(EVENT_DELAY);
        UI.events.triggerEvent(new LabelCreatedEvent());
        sleep(EVENT_DELAY);
        UI.events.triggerEvent(new MilestoneCreatedEvent());
        sleep(EVENT_DELAY);

        click("#dummy/dummy_col0_1");

        // show docs
        press(KeyCode.F1).release(KeyCode.F1);
        press(KeyCode.G).press(KeyCode.H).release(KeyCode.H).release(KeyCode.G);

        // scroll to top
        press(KeyCode.U).release(KeyCode.U);

        // scroll to bottom
        press(KeyCode.N).release(KeyCode.N);

        // scroll up
        press(KeyCode.J).release(KeyCode.J);

        // scroll down
        press(KeyCode.K).release(KeyCode.K);

        // go to labels page
        press(KeyCode.G).press(KeyCode.L).release(KeyCode.L).release(KeyCode.G);

        // go to issues page
        press(KeyCode.G).press(KeyCode.I).release(KeyCode.I).release(KeyCode.G);

        // go to milestones page
        press(KeyCode.G).press(KeyCode.M).release(KeyCode.M).release(KeyCode.G);

        // go to pull requests page
        press(KeyCode.G).press(KeyCode.P).release(KeyCode.P).release(KeyCode.G);

        // go to developers page
        press(KeyCode.G).press(KeyCode.D).release(KeyCode.D).release(KeyCode.G);

        // go to keyboard shortcuts page
        press(KeyCode.G).press(KeyCode.K).release(KeyCode.K).release(KeyCode.G);

        // manage labels
        press(KeyCode.L).release(KeyCode.L);

        // manage assignee
        press(KeyCode.A).release(KeyCode.A);

        // manage milestone
        press(KeyCode.M).release(KeyCode.M);

        // jump to comments
        press(KeyCode.C).release(KeyCode.C);

        click("View");
        click("Documentation");
        click("Preferences");
        click("Logout");
    }
}
