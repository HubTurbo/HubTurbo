package guitests;

import backend.RepoIO;
import javafx.scene.control.ComboBox;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;
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
        Preferences prefs = TestController.createTestPreferences();
        prefs.setLastLoginCredentials("test", "test");
        prefs.setLastViewedRepository("test/test");

        RepoIO testIO = TestController.createTestingRepoIO(Optional.empty());
        testIO.setRepoOpControl(TestUtils.createRepoOpControlWithEmptyModels(testIO));
        try {
            testIO.openRepository("test/test").get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void savedLogin_lastSavedLoginCredentials_shouldAllowLoginWithoutPrompting()
            throws InterruptedException {
        ComboBox<String> repositorySelector = getRepositorySelector();
        assertEquals("test/test", repositorySelector.getValue());
    }
}
