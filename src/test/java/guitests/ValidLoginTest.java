package guitests;

import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.loadui.testfx.GuiTest;
import org.testfx.api.FxToolkit;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import ui.IdGenerator;
import ui.TestController;

public class ValidLoginTest extends UITest {

    @Override
    public void setup() throws TimeoutException {
        FxToolkit.setupApplication(TestUI.class, "--test=true");
    }

    @Test
    public void validLoginTest() throws InterruptedException {
        TextField repoOwnerField = GuiTest.find(IdGenerator.getLoginDialogOwnerFieldIdReference());
        clickOn(repoOwnerField);
        selectAll();
        type("test").push(KeyCode.TAB);
        type("test").push(KeyCode.TAB);
        type("test").push(KeyCode.TAB);
        type("test");
        clickOn("Sign in");
        String title = TestController.getUI().getTitle();
        awaitCondition(() -> "test/test (none)".equals(title), 10);
    }
}
