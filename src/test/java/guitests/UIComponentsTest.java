package guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;

public class UIComponentsTest extends UITest {

    @Test
    public void filterTextFieldTest() {
        click("#dummy/dummy_col0_filterTextField");
        type("is");
        press(KeyCode.ESCAPE).release(KeyCode.ESCAPE);
        type("is");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        press(KeyCode.SHIFT).press(KeyCode.DIGIT9).release(KeyCode.DIGIT9).release(KeyCode.SHIFT);
        press(KeyCode.LEFT).release(KeyCode.LEFT);
        press(KeyCode.TAB).release(KeyCode.TAB);
        press(KeyCode.RIGHT).release(KeyCode.RIGHT);
        type("open OR closed");
        press(KeyCode.SHIFT).press(KeyCode.DIGIT0).release(KeyCode.DIGIT0).release(KeyCode.SHIFT);
        press(KeyCode.ENTER).release(KeyCode.ENTER);
    }

    @Test
    public void navigableListViewTest() {
        click("#dummy/dummy_col0_1");
        press(KeyCode.V).release(KeyCode.V);
        press(KeyCode.T).release(KeyCode.T);
        press(KeyCode.ENTER).release(KeyCode.ENTER);
        click("#dummy/dummy_col0_filterTextField");
        press(KeyCode.SPACE).release(KeyCode.SPACE).press(KeyCode.SPACE).release(KeyCode.SPACE);
    }

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
        click("Open");
        click("Board 1");
        click("Boards");
        click("Delete");
        click("Board 1");
        click("OK");

        click("View");
        click("Refresh");
        click("View");
        click("Force Refresh");
    }
}
