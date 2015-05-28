package guitests;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.junit.Before;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;
import ui.RepositorySelector;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class ValidLoginTest extends UITest {
    @Before
    @Override
    public void setupStage() throws Throwable {
        // add parameters as the second parameter onwards
        FXTestUtils.launchApp(TestUI.class, "--test=true");
        try {
            stage = targetWindow(super.stageFuture.get(25, TimeUnit.SECONDS));
            FXTestUtils.bringToFront(stage);
        } catch (Exception e) {
            throw new RuntimeException("Unable to show stage", e);
        }
    }

    @Test
    public void validLoginTest() throws InterruptedException {
        TextField repoOwnerField = find("#repoOwnerField");
        doubleClick(repoOwnerField);
        doubleClick(repoOwnerField);
        type("HubTurbo").push(KeyCode.TAB);
        type("HubTurbo").push(KeyCode.TAB);
        type("test").push(KeyCode.TAB);
        type("test");
        click("Sign in");
        sleep(2000);
        RepositorySelector repositorySelector = find("#repositorySelector");
        assertEquals(repositorySelector.getText(), "HubTurbo/HubTurbo");
    }
}
