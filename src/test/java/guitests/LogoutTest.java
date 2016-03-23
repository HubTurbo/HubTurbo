package guitests;

import javafx.scene.input.KeyCode;
import org.junit.After;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;
import prefs.Preferences;
import ui.TestController;
import util.FileHelper;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LogoutTest extends UITest {

    String configFileDirectory = TestController.TEST_DIRECTORY;
    String testConfigFileName = TestController.TEST_SESSION_CONFIG_FILENAME;

    @Override
    public void launchApp() {
        FXTestUtils.launchApp(TestUI.class, "--testconfig=true");
    }

    @Test
    public void logoutFunctionTest() {
        selectAll();
        type("dummy").push(KeyCode.TAB);
        type("dummy").push(KeyCode.TAB);
        type("test").push(KeyCode.TAB);
        type("test");
        click("Sign in");
        sleep(2000);

        click("File");
        click("Logout");

        // checking that the json file exists and the saved credentials have been emptied
        File testConfig = new File(configFileDirectory, testConfigFileName);

        if (!(testConfig.exists() && testConfig.isFile())) {
            fail("File not found: " + testConfig.getAbsolutePath());
        }

        Preferences testPref = TestController.loadTestPreferences();
        assertEquals("", testPref.getLastLoginUsername());
        assertEquals("", testPref.getLastLoginPassword());
    }

    @After
    public void teardown() {
        clearAllTestConfigs();
    }
}
