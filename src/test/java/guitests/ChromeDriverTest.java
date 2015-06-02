package guitests;

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
        sleep(1000);
        UI.events.triggerEvent(new IssueCreatedEvent());
        UI.events.triggerEvent(new LabelCreatedEvent());
        UI.events.triggerEvent(new MilestoneCreatedEvent());
        click("View");
        click("Documentation");
    }
}
