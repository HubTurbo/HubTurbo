package guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;
import ui.UI;
import ui.components.KeyboardShortcuts;
import ui.issuecolumn.ColumnControl;
import util.events.ModelUpdatedEventHandler;

import static org.junit.Assert.assertEquals;

public class MenuControlTest extends UITest {

    private boolean modelUpdatedEventTriggered;

    @Test
    public void menuControlTest() {
        modelUpdatedEventTriggered = false;
        UI.events.registerEvent((ModelUpdatedEventHandler) e -> modelUpdatedEventTriggered = true);
        ColumnControl columnControl = (ColumnControl) find("#dummy/dummy_col0").getParent();
        press(KeyCode.CONTROL).press(KeyCode.W).release(KeyCode.W).release(KeyCode.CONTROL);
        assertEquals(0, columnControl.getNumberOfColumns());
        press(KeyCode.CONTROL).press(KeyCode.P).release(KeyCode.P).release(KeyCode.CONTROL);
        assertEquals(1, columnControl.getNumberOfColumns());
        press(KeyCode.CONTROL).press(KeyCode.SHIFT).press(KeyCode.P).release(KeyCode.P)
            .release(KeyCode.SHIFT).release(KeyCode.CONTROL);
        assertEquals(2, columnControl.getNumberOfColumns());

        click("Panels");
        click("Create");
        assertEquals(3, columnControl.getNumberOfColumns());
        click("Panels");
        click("Create (Left)");
        assertEquals(4, columnControl.getNumberOfColumns());
        click("Panels");
        click("Close");
        assertEquals(3, columnControl.getNumberOfColumns());
        click("Panels");
        click("Close");
        assertEquals(2, columnControl.getNumberOfColumns());

        click("Boards");
        click("Save");
        type("1");
        click("OK");
        assertEquals(1, columnControl.getNumberOfSavedBoards());

        press(KeyCode.CONTROL).press(KeyCode.W).release(KeyCode.W).release(KeyCode.CONTROL);
        press(KeyCode.CONTROL).press(KeyCode.W).release(KeyCode.W).release(KeyCode.CONTROL);
        assertEquals(0, columnControl.getNumberOfColumns());

        click("Boards");
        push(KeyCode.DOWN).push(KeyCode.DOWN);
        push(KeyCode.RIGHT);
        push(KeyCode.ENTER);
        assertEquals(2, columnControl.getNumberOfColumns());

        click("Boards");
        push(KeyCode.DOWN).push(KeyCode.DOWN).push(KeyCode.DOWN);
        push(KeyCode.RIGHT);
        push(KeyCode.ENTER);
        click("OK");
        assertEquals(0, columnControl.getNumberOfSavedBoards());

        click("View");
        click("Refresh");
        push(KeyboardShortcuts.REFRESH);
        assertEquals(true, modelUpdatedEventTriggered);
        modelUpdatedEventTriggered = false;
    }
}
