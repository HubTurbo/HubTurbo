package guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;

public class MenuControlTest extends UITest {

    // TODO test that events have been triggered
    @Test
    public void menuControlTest() {
        click("Panels");
        click("Create");
        click("Panels");
        click("Create (Left)");
        click("Panels");
        click("Close");
        click("Panels");
        click("Close");

        click("Boards");
        click("Save");
        type("Board 1");
        click("OK");
        click("Boards");
        press(KeyCode.DOWN).release(KeyCode.DOWN);
        press(KeyCode.DOWN).release(KeyCode.DOWN);
        press(KeyCode.RIGHT).release(KeyCode.RIGHT);
        press(KeyCode.ENTER).release(KeyCode.ENTER);
        click("Boards");
        press(KeyCode.DOWN).release(KeyCode.DOWN);
        press(KeyCode.DOWN).release(KeyCode.DOWN);
        press(KeyCode.DOWN).release(KeyCode.DOWN);
        press(KeyCode.RIGHT).release(KeyCode.RIGHT);
        press(KeyCode.ENTER).release(KeyCode.ENTER);
        click("OK");

        click("View");
        click("Refresh");
        click("View");
        click("Force Refresh");
    }
}
