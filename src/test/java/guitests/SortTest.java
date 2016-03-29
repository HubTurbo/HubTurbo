package guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;

public class SortTest extends UITest {

    // TODO check that issue list is sorted correctly
    @Test
    public void sortTest() {
        click("#dummy/dummy_col0_filterTextField");
        // Ascending ID
        type("sort:id");
        push(KeyCode.ENTER);
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        // Descending ID (default)
        type("sort:-id");
        push(KeyCode.ENTER);
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        // Comment count
        type("sort:comments");
        push(KeyCode.ENTER);
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        // Repo ID (e.g. dummy2/dummy)
        type("sort:repo");
        push(KeyCode.ENTER);
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        // Updated time (ascending)
        type("sort:updated");
        push(KeyCode.ENTER);
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        // Updated time (descending)
        type("sort:-date");
        push(KeyCode.ENTER);
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        // Issue state (open)
        type("sort:status");
        push(KeyCode.ENTER);
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        // Issue state (closed)
        type("sort:-status");
        push(KeyCode.ENTER);
    }
}
