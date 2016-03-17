package guitests;

import backend.RepoIO;
import javafx.scene.control.ComboBox;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;
import prefs.ConfigFileHandler;
import prefs.GlobalConfig;
import prefs.Preferences;
import tests.TestUtils;
import ui.TestController;
import ui.UI;
import ui.components.StatusUIStub;
import util.events.EventDispatcherStub;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class WrongLastViewedTest extends UITest {

    @Override
    public void launchApp() {
        FXTestUtils.launchApp(TestUI.class, "--testconfig=true");
    }

    @Override
    public void beforeStageStarts() {
        UI.status = new StatusUIStub();
        UI.events = new EventDispatcherStub();

        // setup test json with last viewed repo "test/test"
        // but we create a repo json file for "test2/test2" instead and see if it gets loaded
        ConfigFileHandler configFileHandler =
                new ConfigFileHandler(Preferences.DIRECTORY, Preferences.TEST_CONFIG_FILE);
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setLastLoginCredentials("test", "test");
        globalConfig.setLastViewedRepository("test/test");
        configFileHandler.saveGlobalConfig(globalConfig);
        RepoIO testIO = TestController.createTestingRepoIO(Optional.empty());
        testIO.setRepoOpControl(TestUtils.createRepoOpControlWithEmptyModels(testIO));
        try {
            testIO.openRepository("test2/test2").get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void wrongLastViewedTest() throws InterruptedException {
        ComboBox<String> repositorySelector = find("#repositorySelector");
        assertEquals("test2/test2", repositorySelector.getValue());
    }
}
