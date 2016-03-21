package guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;
import ui.IdGenerator;
import ui.UI;
import ui.components.FilterTextField;
import util.events.UpdateProgressEvent;

import static org.junit.Assert.assertEquals;

public class UIComponentsTest extends UITest {

    // TODO check that filter text field does indeed do autocomplete correctly, etc
    @Test
    public void keywordCompletionTest() {
        String filterTextFieldId = IdGenerator.getPanelFilterTextFieldIdForTest(0);
        click(filterTextFieldId);
        selectAll();
        push(KeyCode.BACK_SPACE);
        type("ass");
        push(KeyCode.TAB);
        FilterTextField filterTextField = find(filterTextFieldId);
        assertEquals("assignee", filterTextField.getText());
    }

    @Test
    public void filterTextFieldTest() {
        String filterTextFieldId = IdGenerator.getPanelFilterTextFieldIdForTest(0);
        click(filterTextFieldId);
        selectAll();
        push(KeyCode.BACK_SPACE);
        type("is:open OR is:closed");
        push(KeyCode.ENTER);
<<<<<<< 63e07465fa0e98a2fb3c264f6f7d62998b5259b8

        FilterTextField filterTextField = find("#dummy/dummy_col0_filterTextField");
=======
        
        FilterTextField filterTextField = find(filterTextFieldId);
>>>>>>> Refactored more id strings in ScrollableListViewTests, SortTest, UIComponentsTest, UIEventTests and UseGlobalConfigsTest.
        filterTextField.clear();
        click(filterTextFieldId);
        type("!@#$%^&*( ) { } :?");
        assertEquals("!@#$%^&*( ) { } :?", filterTextField.getText());
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
