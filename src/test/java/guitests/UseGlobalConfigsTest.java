package guitests;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.junit.After;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;
import prefs.GlobalConfig;
import prefs.Preferences;
import ui.RepositorySelector;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class UseGlobalConfigsTest extends UITest {

    String configFileDirectory = Preferences.DIRECTORY;
    String testConfigFileName = Preferences.TEST_CONFIG_FILE;

    @Override
    public void launchApp() {
        FXTestUtils.launchApp(TestUI.class, "--test=true", "--testconfig=true");
    }

    @Test
    public void globalConfigTest() {
        // Cleaning up with @Before creates race condition.
        // Neither can we ensure test config file does not exist before starting test,
        // as the program automatically generates an empty config file if it does not
        // exist.
        TextField repoOwnerField = find("#repoOwnerField");
        doubleClick(repoOwnerField);
        doubleClick(repoOwnerField);
        type("dummy").push(KeyCode.TAB);
        type("dummy").push(KeyCode.TAB);
        type("test").push(KeyCode.TAB);
        type("test");
        click("Sign in");
        sleep(2000);
        RepositorySelector repositorySelector = find("#repositorySelector");
        assertEquals(repositorySelector.getText(), "dummy/dummy");

        // Make a new board
        click("Boards");
        click("Save");
        type("Empty Board");
        press(KeyCode.ENTER).release(KeyCode.ENTER);

        // Load dummy2/dummy2 too
        sleep(2000);
        press(KeyCode.CONTROL).press(KeyCode.P).release(KeyCode.P).release(KeyCode.CONTROL);
        sleep(2000);
        click("#dummy/dummy_col1_filterTextField");
        type("repo");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("dummy2/dummy2");
        press(KeyCode.ENTER).release(KeyCode.ENTER);
        sleep(2000);

        // Make a new board
        click("Boards");
        click("Save");
        type("Dummy Board");
        press(KeyCode.ENTER).release(KeyCode.ENTER);

        // Then exit program...
        click("Preferences");
        click("Logout");

        // ...and check if the test JSON is still there...
        File testConfig = new File(configFileDirectory, testConfigFileName);
        if (!(testConfig.exists() && testConfig.isFile())) fail();

        // ...then check that the JSON file contents are correct.
        Preferences testPref = new Preferences(true);
        // Credentials
        assertEquals("test", testPref.getLastLoginUsername());
        assertEquals("test", testPref.getLastLoginPassword());
        // Last open filters
        List<String> lastOpenFilters = testPref.getLastOpenFilters();
        assertEquals(2, lastOpenFilters.size());
        assertEquals("", lastOpenFilters.get(0));
        assertEquals("repo:dummy2/dummy2", lastOpenFilters.get(1));
        // Last viewed repositories
        List<String> lastViewedRepositories = testPref.getLastViewedRepositories();
        assertEquals("dummy/dummy", lastViewedRepositories.get(0));
        assertEquals("dummy2/dummy2", lastViewedRepositories.get(1));
        // Boards
        Map<String, List<String>> boards = testPref.getAllBoards();
        List<String> emptyBoard = boards.get("Empty Board");
        assertEquals(1, emptyBoard.size());
        assertEquals("", emptyBoard.get(0));
        List<String> dummyBoard = boards.get("Dummy Board");
        assertEquals(2, dummyBoard.size());
        assertEquals("", dummyBoard.get(0));
        assertEquals("repo:dummy2/dummy2", dummyBoard.get(1));
    }

    @After
    public void teardown() {
        File testConfig = new File(configFileDirectory, testConfigFileName);
        if (testConfig.exists() && testConfig.isFile()) testConfig.delete();
    }
}
