package guitests;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import org.junit.After;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;

import prefs.Preferences;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LogoutTest extends UITest {

    String configFileDirectory = Preferences.DIRECTORY;
    String testConfigFileName = Preferences.TEST_CONFIG_FILE;

    @Override
    public void launchApp() {
        FXTestUtils.launchApp(TestUI.class, "--testconfig=true");
    }

    @Test
    public void logoutFunctionTest() {
        TextField repoOwnerField = find("#repoOwnerField");
        doubleClick(repoOwnerField);
        doubleClick(repoOwnerField);
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
        if (!(testConfig.exists() && testConfig.isFile())) fail();

        Preferences testPref = new Preferences(true);
        assertEquals("", testPref.getLastLoginUsername());
        assertEquals("", testPref.getLastLoginPassword());
    }

    @After
    public void teardown() {
        File testConfig = new File(configFileDirectory, testConfigFileName);
        if (testConfig.exists() && testConfig.isFile()) testConfig.delete();
    }
}
