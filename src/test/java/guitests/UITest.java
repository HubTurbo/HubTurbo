package guitests;

import backend.interfaces.RepoStore;
import com.google.common.util.concurrent.SettableFuture;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBoxBase;
import javafx.stage.Stage;
import org.junit.Before;
import org.loadui.testfx.GuiTest;
import org.loadui.testfx.exceptions.NoNodesFoundException;
import org.loadui.testfx.exceptions.NoNodesVisibleException;
import org.loadui.testfx.utils.FXTestUtils;
import prefs.Preferences;
import ui.UI;
import util.PlatformEx;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.google.common.io.Files.getFileExtension;

public class UITest extends GuiTest {

    protected static final SettableFuture<Stage> STAGE_FUTURE = SettableFuture.create();

    protected static class TestUI extends UI {
        public TestUI() {
            super();
        }

        @Override
        public void start(Stage primaryStage) {
            super.start(primaryStage);
            STAGE_FUTURE.set(primaryStage);
        }
    }

    public void setupMethod() {
        // method to be overridden if anything needs to be done (e.g. to the json) before the stage starts
    }

    public static void clearTestFolder() {
        try {
            if (Files.exists(Paths.get(RepoStore.TEST_DIRECTORY))) {
                Files.walk(Paths.get(RepoStore.TEST_DIRECTORY), 1)
                        .filter(Files::isRegularFile)
                        .filter(p ->
                                getFileExtension(String.valueOf(p.getFileName())).equalsIgnoreCase("json") ||
                                getFileExtension(String.valueOf(p.getFileName())).equalsIgnoreCase("json-err")
                        )
                        .forEach(p -> new File(p.toAbsolutePath().toString()).delete());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Before
    @Override
    public void setupStage() throws Throwable {
        // delete test.json if it exists
        File testConfig = new File(Preferences.DIRECTORY, Preferences.TEST_CONFIG_FILE);
        if (testConfig.exists() && testConfig.isFile()) {
            assert testConfig.delete();
        }
        clearTestFolder();
        setupMethod();

        if (stage == null) {
            launchApp();
        }
        try {
            stage = targetWindow(STAGE_FUTURE.get(25, TimeUnit.SECONDS));
            FXTestUtils.bringToFront(stage);
        } catch (Exception e) {
            throw new RuntimeException("Unable to show stage", e);
        }
    }

    // override this to change launch arguments
    public void launchApp() {
        FXTestUtils.launchApp(TestUI.class, "--test=true", "--bypasslogin=true");
    }

    @Override
    protected Parent getRootNode() {
        return stage.getScene().getRoot();
    }

    public void waitUntilNodeAppears(Node node) {
        waitUntil(node, Node::isVisible);
    }

    public void waitUntilNodeDisappears(Node node) {
        waitUntil(node, n -> !n.isVisible());
    }

    public void waitUntilNodeAppears(String selector) {
        while (!existsQuiet(selector)) {
            PlatformEx.waitOnFxThread();
            sleep(100);
        }
    }

    public void waitUntilNodeDisappears(String selector) {
        while (existsQuiet(selector)) {
            PlatformEx.waitOnFxThread();
            sleep(100);
        }
    }

    public <T extends Node> T findOrWaitFor(String selector) {
        waitUntilNodeAppears(selector);
        return find(selector);
    }

    public <T extends Node> Optional<T> findQuiet(String selectorOrText) {
        try {
            return Optional.of(find(selectorOrText));
        } catch (NoNodesFoundException | NoNodesVisibleException e) {
            return Optional.empty();
        }
    }

    private boolean existsQuiet(String selector) {
        try {
            return exists(selector);
        } catch (NoNodesFoundException | NoNodesVisibleException e) {
            return false;
        }
    }

    public <T> void waitForValue(ComboBoxBase<T> comboBoxBase) {
        waitUntil(comboBoxBase, c -> c.getValue() != null);
    }
}
