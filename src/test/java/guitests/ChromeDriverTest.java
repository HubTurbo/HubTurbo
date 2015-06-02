package guitests;

import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;
import ui.UI;
import util.events.IssueSelectedEvent;

public class ChromeDriverTest extends UITest {

    @Override
    public void launchApp() {
        FXTestUtils.launchApp(
                TestUI.class, "--test=true", "--bypasslogin=true", "--testchromedriver=true");
    }

    @Test
    public void chromeDriverStubTest() {
        UI.events.triggerEvent(new IssueSelectedEvent("dummy/dummy", 1, 0));
    }
}
