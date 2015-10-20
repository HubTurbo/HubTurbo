package guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;

public class SortTest extends UITest {

    // TODO check that issue list is sorted correctly
    @Test
    public void sortTest () {
        click("#dummy/dummy_col0_filterTextField");
        // Ascending ID
        type("sort:id");
        push(KeyCode.ENTER);
        doubleClick("#dummy/dummy_col0_filterTextField");
        // Descending ID (default)
        type("sort:-id");
        push(KeyCode.ENTER);
        doubleClick("#dummy/dummy_col0_filterTextField");
        // Comment count
        type("sort:comments");
        push(KeyCode.ENTER);
        doubleClick("#dummy/dummy_col0_filterTextField");
        // Repo ID (e.g. dummy2/dummy)
        type("sort:repo");
        push(KeyCode.ENTER);
        doubleClick("#dummy/dummy_col0_filterTextField");
        // Updated time (ascending)
        type("sort:updated");
        push(KeyCode.ENTER);
        doubleClick("#dummy/dummy_col0_filterTextField");
        // Updated time (descending)
        type("sort:-date");
        push(KeyCode.ENTER);
        doubleClick("#dummy/dummy_col0_filterTextField");
        // Issue state (open)
        type("sort:status");
        push(KeyCode.ENTER);
        doubleClick("#dummy/dummy_col0_filterTextField");
        // Issue state (closed)
        type("sort:-status");
        push(KeyCode.ENTER);
    }
}
