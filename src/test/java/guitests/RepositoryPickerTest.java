package guitests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.loadui.testfx.Assertions.assertNodeExists;

import java.io.File;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.eclipse.egit.github.core.RepositoryId;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;

import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import prefs.ConfigFileHandler;
import prefs.GlobalConfig;
import prefs.Preferences;
import ui.TestController;
import ui.UI;
import util.PlatformEx;
import util.events.testevents.PrimaryRepoChangedEventHandler;

public class RepositoryPickerTest extends UITest {

    private static String primaryRepo;

    protected static class RepositorySelectorTestUI extends UI {
        public RepositorySelectorTestUI() {
            super();
        }

        @Override
        public void start(Stage primaryStage) {
            super.start(primaryStage);
            STAGE_FUTURE.set(primaryStage);
        }

        @Override
        protected void registerTestEvents() {
            UI.events.registerEvent((PrimaryRepoChangedEventHandler) e -> primaryRepo = e.repoId);
        }
    }

    @Override
    public void launchApp() {
        FXTestUtils.launchApp(RepositorySelectorTestUI.class, "--testconfig=true");
    }

    @Override
    public void beforeStageStarts() {
        // setup test json with last viewed repo "dummy/dummy"
        // obviously the json for that repo doesn't exist
        ConfigFileHandler configFileHandler =
                new ConfigFileHandler(Preferences.DIRECTORY, Preferences.TEST_CONFIG_FILE);
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setLastLoginCredentials("test", "test");
        globalConfig.setLastViewedRepository("dummy/dummy");
        configFileHandler.saveGlobalConfig(globalConfig);
    }

    @Test
    public void repositorySelectorTest() {
        // check if test json is present
        File testConfig = new File(Preferences.DIRECTORY, Preferences.TEST_CONFIG_FILE);
        boolean testConfigExists = testConfig.exists() && testConfig.isFile();
        if (!testConfigExists) {
            fail();
        }

        VBox matchingRepositoryList;
        TextField userInputField;

        // now we check if the login dialog pops up because the "dummy/dummy" json
        // doesn't exist and there are no other valid repo json files
        type("dummy").push(KeyCode.TAB);
        type("dummy").push(KeyCode.ENTER);
        push(KeyCode.CONTROL, KeyCode.R);
        matchingRepositoryList = find("#matchingRepositoryList");
        assertEquals(1, matchingRepositoryList.getChildren().size());
        assertEquals("dummy/dummy", primaryRepo);
        push(KeyCode.ESCAPE);

        // we check if the "dummy2/dummy2" is added to the repository selector
        // but the primary repo isn't changed
        Platform.runLater(find("#dummy/dummy_col0_filterTextField")::requestFocus);
        PlatformEx.waitOnFxThread();
        type("repo:dummy2/dummy2");
        push(KeyCode.ENTER);
        push(KeyCode.CONTROL, KeyCode.R);
        matchingRepositoryList = find("#matchingRepositoryList");
        assertEquals(2, matchingRepositoryList.getChildren().size());
        assertEquals("dummy/dummy", primaryRepo);
        push(KeyCode.ESCAPE);

        // we check if "dummy3/dummy3" is added to the repository selector
        // and that the primary repo is also changed
        push(KeyCode.CONTROL, KeyCode.R);
        userInputField = find("#repositoryPickerUserInputField");
        doubleClick(userInputField);
        doubleClick();
        type("dummy3/dummy3");
        push(KeyCode.ENTER);
        push(KeyCode.CONTROL, KeyCode.R);
        matchingRepositoryList = find("#matchingRepositoryList");
        assertEquals(3, matchingRepositoryList.getChildren().size());
        assertEquals("dummy3/dummy3", primaryRepo);
        push(KeyCode.ESCAPE);

        // we check if repo's id with white spaces are handled correctly
        push(KeyCode.CONTROL, KeyCode.R);
        userInputField = find("#repositoryPickerUserInputField");
        doubleClick(userInputField);
        doubleClick();
        type(" dummy4 / dummy4 ");
        push(KeyCode.ENTER);
        push(KeyCode.CONTROL, KeyCode.R);
        matchingRepositoryList = find("#matchingRepositoryList");
        assertEquals(4, matchingRepositoryList.getChildren().size());
        assertEquals("dummy4/dummy4", primaryRepo);
        push(KeyCode.ESCAPE);

        // we check if deleting used repo does not remove it
        traverseMenu("Repos", "Remove", "dummy4/dummy4 [in use, not removable]"); // first used repo
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        push(KeyCode.CONTROL, KeyCode.R);
        matchingRepositoryList = find("#matchingRepositoryList");
        assertEquals(4, matchingRepositoryList.getChildren().size());
        push(KeyCode.ESCAPE);

        // we check if delete repo works
        traverseMenu("Repos", "Remove", "dummy/dummy");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        push(KeyCode.CONTROL, KeyCode.R);
        matchingRepositoryList = find("#matchingRepositoryList");
        assertEquals(3, matchingRepositoryList.getChildren().size());
        push(KeyCode.ESCAPE);

        // we check again if deleting used repo does not remove it
        traverseMenu("Repos", "Remove", "dummy2/dummy2 [in use, not removable]"); // second used repo
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        push(KeyCode.CONTROL, KeyCode.R);
        matchingRepositoryList = find("#matchingRepositoryList");
        assertEquals(3, matchingRepositoryList.getChildren().size());
        push(KeyCode.ESCAPE);

        // exit program
        traverseMenu("File", "Quit");
        push(KeyCode.ENTER);

        // testing that the correct repo was saved in the json
        // check if the test JSON is still there...
        if (!(testConfig.exists() && testConfig.isFile())) {
            fail();
        }

        // ...then check that the JSON file contents are correct.
        Preferences testPref = TestController.loadTestPreferences();
        // Last viewed repository
        RepositoryId lastViewedRepository = testPref.getLastViewedRepository().get();
        assertEquals("dummy4/dummy4", lastViewedRepository.generateId());
    }
}
