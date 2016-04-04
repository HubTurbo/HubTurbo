package guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;
import org.testfx.api.FxToolkit;

import static org.loadui.testfx.Assertions.assertNodeExists;
import static org.loadui.testfx.controls.Commons.hasText;

import java.util.concurrent.TimeoutException;

public class InvalidLoginTest extends UITest {

    @Override
    public void setup() throws TimeoutException {
        FxToolkit.setupApplication(TestUI.class, "--test=true");
    }

    @Test
    public void invalidLoginTest() throws InterruptedException {
        selectAll();
        push(KeyCode.BACK_SPACE);
        type("abc").push(KeyCode.TAB);
        push(KeyCode.BACK_SPACE);
        type("abc").push(KeyCode.TAB);
        push(KeyCode.BACK_SPACE);
        type("abc").push(KeyCode.TAB);
        push(KeyCode.BACK_SPACE);
        type("abc");
        clickOn("Sign in");
        sleep(1000);
        assertNodeExists(hasText("Failed to sign in. Please try again."));
        clickOn("OK");
    }

    @Test
    public void emptyLoginTest() throws InterruptedException {
        selectAll();
        push(KeyCode.BACK_SPACE);
        type("abc").push(KeyCode.TAB);
        push(KeyCode.BACK_SPACE);
        type("abc").push(KeyCode.TAB);
        push(KeyCode.BACK_SPACE);
        push(KeyCode.TAB).push(KeyCode.BACK_SPACE);
        clickOn("Sign in");
        sleep(1000);
        assertNodeExists(hasText("Failed to sign in. Please try again."));
        clickOn("OK");
    }

    @Test
    public void emptyUsernameTest() throws InterruptedException {
        selectAll();
        push(KeyCode.BACK_SPACE);
        type("abc").push(KeyCode.TAB);
        push(KeyCode.BACK_SPACE);
        type("abc").push(KeyCode.TAB);
        push(KeyCode.BACK_SPACE).push(KeyCode.TAB);
        push(KeyCode.BACK_SPACE);
        type("abc");
        clickOn("Sign in");
        sleep(1000);
        assertNodeExists(hasText("Failed to sign in. Please try again."));
        clickOn("OK");
    }

    @Test
    public void emptyPasswordTest() throws InterruptedException {
        selectAll();
        push(KeyCode.BACK_SPACE);
        type("abc").push(KeyCode.TAB);
        push(KeyCode.BACK_SPACE);
        type("abc").push(KeyCode.TAB);
        push(KeyCode.BACK_SPACE);
        type("abc").push(KeyCode.TAB);
        push(KeyCode.BACK_SPACE);
        clickOn("Sign in");
        sleep(1000);
        assertNodeExists(hasText("Failed to sign in. Please try again."));
        clickOn("OK");
    }

}
