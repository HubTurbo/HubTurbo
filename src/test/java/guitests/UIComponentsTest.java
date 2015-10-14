package guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;
import ui.UI;
import ui.components.FilterTextField;
import util.events.UpdateProgressEvent;

import static org.junit.Assert.assertEquals;

public class UIComponentsTest extends UITest {
    
    private UtilMethods util = new UtilMethods();
    // TODO check that filter text field does indeed do autocomplete correctly, etc
    @Test
    public void keywordCompletionTest() {
        doubleClick("#dummy/dummy_col0_filterTextField");
        click("#dummy/dummy_col0_filterTextField");
        push(KeyCode.BACK_SPACE);
        type("ass");
        push(KeyCode.TAB);
        FilterTextField filterTextField = find("#dummy/dummy_col0_filterTextField");
        assertEquals("assignee", filterTextField.getText());
    }

    @Test
    public void filterTextFieldTest() {
        doubleClick("#dummy/dummy_col0_filterTextField");
        push(KeyCode.BACK_SPACE);
        util.typeString("(is:open OR is:closed)");
        push(KeyCode.ENTER);
    }

    // TODO check that progress bar is updating
    @Test
    public void textProgressBarTest() {
        UI.events.triggerEvent(new UpdateProgressEvent("dummy/dummy", 0));
        sleep(1000);
        UI.events.triggerEvent(new UpdateProgressEvent("dummy/dummy", 0.5f));
        sleep(1000);
        UI.events.triggerEvent(new UpdateProgressEvent("dummy/dummy", 0.9f));
        sleep(1000);
        UI.events.triggerEvent(new UpdateProgressEvent("dummy/dummy"));
    }

}
