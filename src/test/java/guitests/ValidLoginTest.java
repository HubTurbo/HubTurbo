package guitests;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;
import ui.RepositorySelector;

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
