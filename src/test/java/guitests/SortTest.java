package guitests;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;
import ui.issuepanel.IssuePanel;

import static org.junit.Assert.assertEquals;

public class SortTest extends UITest {

    @Test
    public void sortTest () {
        click("#dummy/dummy_col0_filterTextField");
        // Ascending ID
        type("sort");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("id");
        press(KeyCode.ENTER).release(KeyCode.ENTER);
        doubleClick("#dummy/dummy_col0_filterTextField");
        doubleClick("#dummy/dummy_col0_filterTextField");
        // Descending ID (default)
        type("sort");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("-id");
        press(KeyCode.ENTER).release(KeyCode.ENTER);
        click("#dummy/dummy_col0_1");
        doubleClick("#dummy/dummy_col0_filterTextField");
        doubleClick("#dummy/dummy_col0_filterTextField");
        // Comment count
        type("sort");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("comments");
        press(KeyCode.ENTER).release(KeyCode.ENTER);
        doubleClick("#dummy/dummy_col0_filterTextField");
        doubleClick("#dummy/dummy_col0_filterTextField");
        // Repo ID (e.g. dummy2/dummy)
        type("sort");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("repo");
        press(KeyCode.ENTER).release(KeyCode.ENTER);
        // Updated time (ascending)
        doubleClick("#dummy/dummy_col0_filterTextField");
        doubleClick("#dummy/dummy_col0_filterTextField");
        type("sort");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("updated");
        press(KeyCode.ENTER).release(KeyCode.ENTER);
        // Updated time (descending)
        doubleClick("#dummy/dummy_col0_filterTextField");
        doubleClick("#dummy/dummy_col0_filterTextField");
        type("sort");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("-date");
        press(KeyCode.ENTER).release(KeyCode.ENTER);
    }
}
