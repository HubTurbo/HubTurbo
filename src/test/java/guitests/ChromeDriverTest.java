package guitests;

import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;

public class ChromeDriverTest extends UITest {

    @Override
    public void launchApp() {
        FXTestUtils.launchApp(
                TestUI.class, "--test=true", "--bypasslogin=true", "--testchromedriver=true");
    }

    @Test
    public void chromeDriverStubTest() {

    }
}
