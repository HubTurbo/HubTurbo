package guitests;

import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.eclipse.egit.github.core.RepositoryId;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;
import prefs.ConfigFileHandler;
import prefs.GlobalConfig;
import prefs.Preferences;
import ui.UI;
import util.events.testevents.PrimaryRepoChangedEventHandler;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.loadui.testfx.Assertions.assertNodeExists;

public class RepositorySelectorTest extends UITest {

    private static String primaryRepo;

    protected static class RepositorySelectorTestUI extends UI {
        public RepositorySelectorTestUI() {
            super();
        }

        @Override
        public void start(Stage primaryStage) {
            super.start(primaryStage);
            stageFuture.set(primaryStage);
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
    public void setupMethod() {
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
        if (!(testConfig.exists() && testConfig.isFile())) fail();

        // now we check if the login dialog pops up because the "dummy/dummy" json
        // doesn't exist and there are no other valid repo json files
        assertNodeExists("#repoOwnerField");
        type("dummy").push(KeyCode.TAB).type("dummy").push(KeyCode.ENTER);
        ComboBox<String> comboBox = find("#repositorySelector");
        assertEquals(1, comboBox.getItems().size());
        assertEquals("dummy/dummy", primaryRepo);

        // we check if the "dummy2/dummy2" is added to the repository selector
        // but the primary repo isn't changed
        click("#dummy/dummy_col0_filterTextField");
        type("repo");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("dummy2/dummy2");
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

        // exit program
        click("File");
        click("Quit");

        // testing that the correct repo was saved in the json
        // check if the test JSON is still there...
        if (!(testConfig.exists() && testConfig.isFile())) fail();

        // ...then check that the JSON file contents are correct.
        Preferences testPref = new Preferences(true);
        // Last viewed repository
        RepositoryId lastViewedRepository = testPref.getLastViewedRepository().get();
        assertEquals("dummy3/dummy3", lastViewedRepository.generateId());
    }

}
