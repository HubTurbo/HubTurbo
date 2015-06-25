package guitests;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.junit.After;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;
import prefs.Preferences;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ViewedRepositoriesTest extends UITest {

    String configFileDirectory = Preferences.DIRECTORY;
    String testConfigFileName = Preferences.TEST_CONFIG_FILE;

    @Override
    public void launchApp() {
        FXTestUtils.launchApp(TestUI.class, "--testconfig=true");
    }

    @Test
    public void globalConfigTest() {
        TextField repoOwnerField = find("#repoOwnerField");
        doubleClick(repoOwnerField);
        doubleClick(repoOwnerField);
        type("dummy").push(KeyCode.TAB);
        type("dummy").push(KeyCode.TAB);
        type("test").push(KeyCode.TAB);
        type("test");
        click("Sign in");
        sleep(2000);
        
        // Testing combo box edit
        doubleClick("#repositorySelector");
        doubleClick();
        type("dummy2/dummy2");
        push(KeyCode.ENTER);

        // Testing filter
        click("#dummy/dummy_col0_filterTextField");
        type("repo");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("dummy1/dummy1");
        push(KeyCode.ENTER);
        sleep(2000);
        
        // Testing combo box select
        click("#repositorySelector");
        moveBy(120, 0);
        click();
        moveBy(-120, 20);
        click();

        click("Preferences");
        click("Quit");

        Preferences testPref = new Preferences(true);
        List<String> lastViewedRepositories = testPref.getLastViewedRepositories();
        assertEquals(3, lastViewedRepositories.size());
        assertEquals("dummy2/dummy2", lastViewedRepositories.get(0));
        assertEquals("dummy1/dummy1", lastViewedRepositories.get(1));
        assertEquals("dummy/dummy", lastViewedRepositories.get(2));
    }

    @After
    public void teardown() {
        File testConfig = new File(configFileDirectory, testConfigFileName);
        if (testConfig.exists() && testConfig.isFile()) testConfig.delete();
    }
}
