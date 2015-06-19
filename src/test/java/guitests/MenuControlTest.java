package guitests;

import org.junit.Test;

import javafx.scene.input.KeyCode;

public class MenuControlTest extends UITest {

    // TODO test that events have been triggered
    @Test
    public void menuControlTest() {
        press(KeyCode.CONTROL).press(KeyCode.W).release(KeyCode.W).release(KeyCode.CONTROL);
        press(KeyCode.CONTROL).press(KeyCode.P).release(KeyCode.P).release(KeyCode.CONTROL);
        press(KeyCode.CONTROL).press(KeyCode.SHIFT).press(KeyCode.P).release(KeyCode.P)
            .release(KeyCode.SHIFT).release(KeyCode.CONTROL);

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
        type("1");
        click("OK");
        click("Boards");
        push(KeyCode.DOWN).push(KeyCode.DOWN);
        push(KeyCode.RIGHT);
        push(KeyCode.ENTER);
        click("Boards");
        push(KeyCode.DOWN).push(KeyCode.DOWN).push(KeyCode.DOWN);
        push(KeyCode.RIGHT);
        push(KeyCode.ENTER);
        click("OK");

        click("View");
        click("Refresh");
        push(KeyCode.F5);
    }
}
