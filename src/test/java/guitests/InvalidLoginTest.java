package guitests;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.junit.Before;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;

import java.util.concurrent.TimeUnit;

public class InvalidLoginTest extends UITest {
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
    public void invalidLoginTest() throws InterruptedException {
        TextField repoOwnerField = find("#repoOwnerField");
        doubleClick(repoOwnerField);
        doubleClick(repoOwnerField);
        type("HubTurbo").push(KeyCode.TAB);
        type("HubTurbo").push(KeyCode.TAB);
        type("HubTurbo").push(KeyCode.TAB);
        type("HubTurbo");
        click("Sign in");
//        sleep(2000);
//        assertNotNull(hasText("Failed to sign in. Please try again."));
    }
}
