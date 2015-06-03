package guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;
import ui.UI;
import util.events.IssueCreatedEvent;
import util.events.IssueSelectedEvent;
import util.events.LabelCreatedEvent;
import util.events.MilestoneCreatedEvent;

public class ChromeDriverTest extends UITest {

    @Override
    public void launchApp() {
        FXTestUtils.launchApp(
                TestUI.class, "--test=true", "--bypasslogin=true", "--testchromedriver=true");
    }

    @Test
    public void chromeDriverStubTest() {
        UI.events.triggerEvent(new IssueSelectedEvent("dummy/dummy", 1, 0));
        UI.events.triggerEvent(new IssueCreatedEvent());
        UI.events.triggerEvent(new LabelCreatedEvent());
        UI.events.triggerEvent(new MilestoneCreatedEvent());

        // jump to comments
        sleep(1000);
        click("#dummy/dummy_col0_1");
        press(KeyCode.C).release(KeyCode.C);

        // show docs
        press(KeyCode.F1).release(KeyCode.F1);

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

        click("View");
        click("Documentation");
        click("Preferences");
        click("Logout");
    }
}
