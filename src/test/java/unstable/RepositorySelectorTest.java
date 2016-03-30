package unstable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.loadui.testfx.Assertions.assertNodeExists;

import java.io.File;

import javafx.application.Platform;
import org.eclipse.egit.github.core.RepositoryId;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;

import guitests.UITest;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import prefs.Preferences;
import ui.IdGenerator;
import ui.TestController;
import ui.UI;
import util.PlatformEx;
import util.events.testevents.PrimaryRepoChangedEventHandler;

public class RepositorySelectorTest extends UITest {

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
        Preferences prefs = TestController.createTestPreferences();
        prefs.setLastLoginCredentials("test", "test");
        prefs.setLastViewedRepository("dummy/dummy");
    }

    @Test
    public void repositorySelectorTest() {
        // check if test json is present
        File testConfig = new File(TestController.TEST_DIRECTORY, TestController.TEST_SESSION_CONFIG_FILENAME);
        boolean testConfigExists = testConfig.exists() && testConfig.isFile();
        if (!testConfigExists) {
            fail();
        }

        // now we check if the login dialog pops up because the "dummy/dummy" json
        // doesn't exist and there are no other valid repo json files
        assertNodeExists(IdGenerator.getLoginDialogOwnerFieldIdReference());
        type("dummy").push(KeyCode.TAB);
        type("dummy").push(KeyCode.ENTER);
        ComboBox<String> comboBox = getRepositorySelector();
        assertEquals(1, comboBox.getItems().size());
        assertEquals("dummy/dummy", primaryRepo);

        // we check if the "dummy2/dummy2" is added to the repository selector
        // but the primary repo isn't changed
        Platform.runLater(getFilterTextFieldAtPanel(0)::requestFocus);
        PlatformEx.waitOnFxThread();
        type("repo:dummy2/dummy2");
        push(KeyCode.ENTER);
        assertEquals(2, comboBox.getItems().size());
        assertEquals("dummy/dummy", primaryRepo);

        // we check if "dummy3/dummy3" is added to the repository selector
        // and that the primary repo is also changed
        doubleClick(comboBox);
        doubleClick();
        type("dummy3/dummy3");
        push(KeyCode.ENTER);
        assertEquals(3, comboBox.getItems().size());
        assertEquals("dummy3/dummy3", primaryRepo);

        // we check if repo's id with white spaces are handled correctly
        doubleClick(comboBox);
        doubleClick();
        type(" dummy4 / dummy4 ");
        push(KeyCode.ENTER);
        assertEquals(4, comboBox.getItems().size());
        assertEquals("dummy4/dummy4", primaryRepo);

        // we check if deleting used repo does not remove it
        traverseMenu("Repos", "Remove", "dummy4/dummy4 [in use, not removable]"); // first used repo
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        assertEquals(4, comboBox.getItems().size());

        // we check if delete repo works
        traverseMenu("Repos", "Remove", "dummy/dummy");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        assertEquals(3, comboBox.getItems().size());

        // we check again if deleting used repo does not remove it
        traverseMenu("Repos", "Remove", "dummy2/dummy2 [in use, not removable]"); // second used repo
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        assertEquals(3, comboBox.getItems().size());

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
