package guitests;

import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.eclipse.egit.github.core.RepositoryId;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;
import prefs.Preferences;
import ui.UI;
import util.events.testevents.PrimaryRepoChangedEventHandler;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
        FXTestUtils.launchApp(RepositorySelectorTestUI.class, "--testconfig=true", "--bypasslogin=true");
    }

    @Test
    public void repositorySelectorTest() {
        ComboBox<String> comboBox = find("#repositorySelector");
        assertEquals(1, comboBox.getItems().size());
        assertEquals("dummy/dummy", primaryRepo);
        click("#dummy/dummy_col0_filterTextField");
        type("repo");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("dummy2/dummy2");
        push(KeyCode.ENTER);
        assertEquals(2, comboBox.getItems().size());
        assertEquals("dummy/dummy", primaryRepo);
        doubleClick(comboBox);
        doubleClick();
        type("dummy3/dummy3");
        push(KeyCode.ENTER);
        assertEquals(3, comboBox.getItems().size());
        assertEquals("dummy3/dummy3", primaryRepo);

        // exit program
        click("Preferences");
        click("Quit");

        // testing that the correct repo was saved in the json
        // check if the test JSON is still there...
        File testConfig = new File(Preferences.DIRECTORY, Preferences.TEST_CONFIG_FILE);
        if (!(testConfig.exists() && testConfig.isFile())) fail();

        // ...then check that the JSON file contents are correct.
        Preferences testPref = new Preferences(true);
        // Last viewed repository
        RepositoryId lastViewedRepository = testPref.getLastViewedRepository().get();
        assertEquals("dummy3/dummy3", lastViewedRepository.generateId());
    }

}
