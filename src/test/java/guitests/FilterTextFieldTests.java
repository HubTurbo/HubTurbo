package guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;
import ui.components.FilterTextField;
import ui.listpanel.ListPanel;

import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class FilterTextFieldTests extends UITest {

    @Test
    public void doubleSpaceTest() {
        ListPanel listPanel = find("#dummy/dummy_col0");
        assertEquals(10, listPanel.getIssueCount());

        FilterTextField filterTextField = find("#dummy/dummy_col0_filterTextField");
        click(filterTextField);

        type("is");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("issue");
        push(KeyCode.ENTER);
        assertEquals(9, listPanel.getIssueCount());
        assertEquals("is:issue", listPanel.getCurrentFilterString());

        push(KeyCode.LEFT).push(KeyCode.LEFT);
        push(KeyCode.SPACE);
        assertEquals("is:iss ue", listPanel.getCurrentFilterString());

        push(KeyCode.BACK_SPACE);
        assertEquals("is:issue", listPanel.getCurrentFilterString());
        try {
            listPanel.getSelectedIssue();
            fail();
        } catch (NoSuchElementException e) {
            // expected behaviour since no issue has been selected yet
        }
        push(KeyCode.SPACE).push(KeyCode.SPACE);
        push(KeyCode.DOWN);
        assertEquals("is:issue", listPanel.getCurrentFilterString());
        assertEquals(9, listPanel.getSelectedIssue().getId());
    }

}
