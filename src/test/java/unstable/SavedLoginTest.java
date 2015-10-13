package unstable;

import backend.RepoIO;
import guitests.UITest;
import javafx.scene.control.ComboBox;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;
import prefs.ConfigFileHandler;
import prefs.GlobalConfig;
import prefs.Preferences;
import ui.UI;
import ui.components.StatusUIStub;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class SavedLoginTest extends UITest {

    @Override
    public void launchApp() {
        FXTestUtils.launchApp(TestUI.class, "--testconfig=true", "--testjson=true");
    }

    @Override
    public void setupMethod() {
        UI.status = new StatusUIStub(); // to avoid NPE
        // setup test json with last viewed repo "test/test"
        // and then create the corresponding repo json file
        ConfigFileHandler configFileHandler =
                new ConfigFileHandler(Preferences.DIRECTORY, Preferences.TEST_CONFIG_FILE);
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setLastLoginCredentials("test", "test");
        globalConfig.setLastViewedRepository("test/test");
        configFileHandler.saveGlobalConfig(globalConfig);
        RepoIO testIO = new RepoIO(true, true);
        try {
            testIO.openRepository("test/test").get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void savedLoginTest() throws InterruptedException {
        ComboBox<String> repositorySelector = find("#repositorySelector");
        assertEquals("test/test", repositorySelector.getValue());
    }
}
