package guitests;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.eclipse.egit.github.core.RepositoryId;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;
import prefs.Preferences;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class UseGlobalConfigsTest extends UITest {

    @Override
    public void launchApp() {
        // isTestMode in UI checks for testconfig too so we don't need to specify --test=true here.
        FXTestUtils.launchApp(TestUI.class, "--testconfig=true");
    }

    @Test
    public void globalConfigTest() {
        // Override setupMethod() if you want to do stuff to the JSON beforehand
        TextField repoOwnerField = find("#repoOwnerField");
        doubleClick(repoOwnerField);
        doubleClick(repoOwnerField);
        type("dummy").push(KeyCode.TAB);
        type("dummy").push(KeyCode.TAB);
        type("test").push(KeyCode.TAB);
        type("test");
        click("Sign in");
        sleep(2000);
        ComboBox<String> repositorySelector = find("#repositorySelector");
        assertEquals(repositorySelector.getValue(), "dummy/dummy");

        // Make a new board
        click("Boards");
        click("Save");
        // Somehow the text field cannot be populated by typing on the CI, use setText instead.
        // TODO find out why
        ((TextField) find("#boardnameinput")).setText("Empty Board");
        click("OK");

        // Load dummy2/dummy2 too
        press(KeyCode.CONTROL).press(KeyCode.P).release(KeyCode.P).release(KeyCode.CONTROL);
        sleep(500);
        click("#dummy/dummy_col1_filterTextField");
        type("repo");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("dummy2/dummy2");
        push(KeyCode.ENTER);
        sleep(2000);

        // Make a new board
        click("Boards");
        click("Save");
        ((TextField) find("#boardnameinput")).setText("Dummy Board");
        click("OK");

        // Then exit program...
        click("File");
        click("Quit");

        // ...and check if the test JSON is still there...
        File testConfig = new File(Preferences.DIRECTORY, Preferences.TEST_CONFIG_FILE);
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
        // Last viewed repository
        RepositoryId lastViewedRepository = testPref.getLastViewedRepository().get();
        assertEquals("dummy/dummy", lastViewedRepository.generateId());
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

}
