package guitests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Test;
import org.testfx.api.FxToolkit;

import javafx.scene.input.KeyCode;
import prefs.Preferences;
import ui.TestController;

public class LogoutTest extends UITest {

    private static final int EVENT_DELAY = 2000;
    String configFileDirectory = TestController.TEST_DIRECTORY;
    String testConfigFileName = TestController.TEST_SESSION_CONFIG_FILENAME;

    @Override
    public void setup() throws TimeoutException {
        FxToolkit.setupApplication(TestUI.class, "--testconfig=true");
    }

    @Test
    public void logoutFunctionTest() {
        selectAll();
        type("dummy").push(KeyCode.TAB);
        type("dummy").push(KeyCode.TAB);
        type("test").push(KeyCode.TAB);
        type("test");

        clickOn("Sign in");
        sleep(EVENT_DELAY);

        clickOn("App");
        clickOn("Logout");

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
