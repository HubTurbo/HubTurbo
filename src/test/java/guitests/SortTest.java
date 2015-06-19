package guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;

public class SortTest extends UITest {

    @Test
    public void sortTest () {
        click("#dummy/dummy_col0_filterTextField");
        // Ascending ID
        type("sort");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("id");
        push(KeyCode.ENTER);
        doubleClick("#dummy/dummy_col0_filterTextField");
        doubleClick("#dummy/dummy_col0_filterTextField");
        // Descending ID (default)
        type("sort");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("-id");
        push(KeyCode.ENTER);
        click("#dummy/dummy_col0_1");
        doubleClick("#dummy/dummy_col0_filterTextField");
        doubleClick("#dummy/dummy_col0_filterTextField");
        // Comment count
        type("sort");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("comments");
        push(KeyCode.ENTER);
        doubleClick("#dummy/dummy_col0_filterTextField");
        doubleClick("#dummy/dummy_col0_filterTextField");
        // Repo ID (e.g. dummy2/dummy)
        type("sort");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("repo");
        push(KeyCode.ENTER);
        // Updated time (ascending)
        doubleClick("#dummy/dummy_col0_filterTextField");
        doubleClick("#dummy/dummy_col0_filterTextField");
        type("sort");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("updated");
        push(KeyCode.ENTER);
        // Updated time (descending)
        doubleClick("#dummy/dummy_col0_filterTextField");
        doubleClick("#dummy/dummy_col0_filterTextField");
        type("sort");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("-date");
        push(KeyCode.ENTER);
    }
}
