package guitests;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;

import static org.junit.Assert.assertEquals;

public class ValidLoginTest extends UITest {

    @Override
    public void launchApp() {
        FXTestUtils.launchApp(TestUI.class, "--test=true");
    }

    @Test
    public void validLoginTest() throws InterruptedException {
        TextField repoOwnerField = find("#repoOwnerField");
        doubleClick(repoOwnerField);
        doubleClick(repoOwnerField);
        type("test").push(KeyCode.TAB);
        type("test").push(KeyCode.TAB);
        type("test").push(KeyCode.TAB);
        type("test");
        click("Sign in");
        sleep(2000);
        ComboBox<String> repositorySelector = find("#repositorySelector");
        assertEquals("test/test", repositorySelector.getValue());
    }
}
