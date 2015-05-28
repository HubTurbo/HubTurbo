package guitests;

import com.google.common.util.concurrent.SettableFuture;
import javafx.scene.Parent;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.Test;
import org.loadui.testfx.GuiTest;
import org.loadui.testfx.utils.FXTestUtils;
import ui.RepositorySelector;
import ui.UI;
import util.events.UILogicRefreshEvent;
import util.events.UpdateDummyRepoEvent;

import java.util.concurrent.TimeUnit;

public class TestGuiTest extends GuiTest {

    private static final SettableFuture<Stage> stageFuture = SettableFuture.create();

    protected static class TestUI extends UI {
        public TestUI() {
            super();
        }

        @Override
        public void start(Stage primaryStage) {
            super.start(primaryStage);
            stageFuture.set(primaryStage);
        }

        @Override
        public void quit() {
            super.quit();
        }
    }

    @Before
    @Override
    public void setupStage() throws Throwable {
        FXTestUtils.launchApp(TestUI.class, "--test=true", "--bypasslogin=true"); // You can add start parameters here
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
    public void clickAround() throws InterruptedException {
        RepositorySelector repositorySelector = find("#repositorySelector");
        click(repositorySelector);
        for (int i = 0; i < 10; i++) {
            click("View");
            click("Refresh");
            sleep(500);
            UI.events.triggerEvent(new UpdateDummyRepoEvent(UpdateDummyRepoEvent.UpdateType.NEW_ISSUE));
            UI.events.triggerEvent(new UILogicRefreshEvent());
            sleep(500);
        }
        sleep(1000);
    }
}
