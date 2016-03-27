package guitests;

import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import ui.IdGenerator;

public class ValidLoginTest extends UITest {

    @Override
    public void launchApp() {
        FXTestUtils.launchApp(TestUI.class, "--test=true");
    }

    @Test
    public void validLoginTest() throws InterruptedException {
        TextField repoOwnerField = find(IdGenerator.getLoginDialogOwnerFieldIdReference());
        click(repoOwnerField);
        selectAll();
        type("test").push(KeyCode.TAB);
        type("test").push(KeyCode.TAB);
        type("test").push(KeyCode.TAB);
        type("test");
        click("Sign in");
        ComboBox<String> repositorySelector = findOrWaitFor(IdGenerator.getRepositorySelectorIdReference());
        awaitCondition(() -> "test/test".equals(repositorySelector.getValue()));
    }
}
