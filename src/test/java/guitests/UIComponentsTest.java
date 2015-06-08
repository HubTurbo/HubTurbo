package guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;
import ui.UI;
import util.events.UpdateProgressEvent;

public class UIComponentsTest extends UITest {

    // TODO check that filter text field does indeed do autocomplete correctly, etc
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

    // TODO check that top issue is indeed highlighted
    @Test
    public void navigableListViewTest() {
        click("#dummy/dummy_col0_1");
        press(KeyCode.V).release(KeyCode.V);
        press(KeyCode.T).release(KeyCode.T);
        press(KeyCode.ENTER).release(KeyCode.ENTER);
        click("#dummy/dummy_col0_filterTextField");
        press(KeyCode.SPACE).release(KeyCode.SPACE).press(KeyCode.SPACE).release(KeyCode.SPACE);
    }

    // TODO check that progress bar is updating
    @Test
    public void textProgressBarTest() {
        UI.events.triggerEvent(new UpdateProgressEvent("dummy/dummy", 0));
        sleep(1000);
        UI.events.triggerEvent(new UpdateProgressEvent("dummy/dummy", 0.5f));
        sleep(1000);
        UI.events.triggerEvent(new UpdateProgressEvent("dummy/dummy", 1));
        sleep(1000);
    }

}
