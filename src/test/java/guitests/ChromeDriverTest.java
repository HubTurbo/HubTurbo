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

    private static final int EVENT_DELAY = 100;

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
        push(KeyCode.F1);
        press(KeyCode.G).press(KeyCode.H).release(KeyCode.H).release(KeyCode.G);

        // scroll to top
        push(KeyCode.I);

        // scroll to bottom
        push(KeyCode.N);

        // scroll up
        push(KeyCode.J);

        // scroll down
        push(KeyCode.K);

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
        push(KeyCode.L);

        // manage assignee
        push(KeyCode.A);

        // manage milestone
        push(KeyCode.M);

        // jump to comments
        push(KeyCode.C);

        click("View");
        click("Documentation");
        click("Preferences");
        click("Logout");
    }
}
