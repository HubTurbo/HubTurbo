package guitests;

import com.google.common.util.concurrent.SettableFuture;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.Test;
import org.loadui.testfx.GuiTest;
import org.loadui.testfx.utils.FXTestUtils;
import ui.UI;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class TestGuiTest extends GuiTest {

    private static final SettableFuture<Stage> stageFuture = SettableFuture.create();

    protected static class TestUI extends UI {
        public TestUI() {
            super();
        }

        @Override
        public void start(Stage primaryStage) throws IOException {
            super.start(primaryStage);
            stageFuture.set(primaryStage);
        }
    }

    @Before
    @Override
    public void setupStage() throws Throwable {
//        assumeTrue(!UserInputDetector.instance.hasDetectedUserInput());

        FXTestUtils.launchApp(TestUI.class); // You can add start parameters here
        try {
            stage = targetWindow(stageFuture.get(25, TimeUnit.SECONDS));
            FXTestUtils.bringToFront(stage);
        } catch (Exception e) {
            throw new RuntimeException("Unable to show stage", e);
        }
    }

    @Override
    protected Parent getRootNode() {
        return stage.getScene().getRoot();
    }

    @Test
    public void testLogin() throws InterruptedException {
        for (int i = 0; i < 1; i++) {
//            type("HubTurbo").push(KeyCode.TAB);
//            type("HubTurbo").push(KeyCode.TAB);
            sleep(5000);
//            type("test").push(KeyCode.TAB);
//            type("test");
            click("Sign in");
//            sleep(1000);
            push(KeyCode.ENTER);
            sleep(5000);
            click("Preferences");
            click("Logout");
        }
    }
}
