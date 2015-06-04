package guitests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import ui.RepositorySelector;

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
        sleep(1000);
        RepositorySelector repositorySelector = find("#repositorySelector");
        assertEquals(repositorySelector.getText(), "test/test");
    }
}
