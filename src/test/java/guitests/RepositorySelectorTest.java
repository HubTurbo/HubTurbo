package guitests;

import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;
import ui.UI;
import util.events.testevents.PrimaryRepoChangedEventHandler;

import static org.junit.Assert.assertEquals;

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
        FXTestUtils.launchApp(RepositorySelectorTestUI.class, "--test=true", "--bypasslogin=true");
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
    }

}
