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
import ui.components.StatusUI;
import util.events.EventDispatcher;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class SavedLoginTest extends UITest {

    @Override
    public void launchApp() {
        FXTestUtils.launchApp(TestUI.class, "--testconfig=true");
    }

    @Override
    public void beforeStageStarts() {
        UI.status = mock(StatusUI.class);
        UI.events = mock(EventDispatcher.class);
        // setup test json with last viewed repo "test/test"
        // and then create the corresponding repo json file
        ConfigFileHandler configFileHandler =
            new ConfigFileHandler(Preferences.DIRECTORY, Preferences.TEST_CONFIG_FILE);
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setLastLoginCredentials("test", "test");
        globalConfig.setLastViewedRepository("test/test");
        configFileHandler.saveGlobalConfig(globalConfig);
        RepoIO testIO = TestController.createTestingRepoIO(Optional.empty());
        TestUtils.createTestRepoOpControl(testIO);
        try {
            testIO.openRepository("test/test").get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void savedLogin_lastSavedLoginCredentials_shouldAllowLoginWithoutPrompting()
        throws InterruptedException {
        ComboBox<String> repositorySelector = find("#repositorySelector");
        assertEquals("test/test", repositorySelector.getValue());
    }
}
