package guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;

public class SortTest extends UITest {

    // TODO check that issue list is sorted correctly
    private UtilMethods util = new UtilMethods();
    @Test
    public void sortTest () {
        click("#dummy/dummy_col0_filterTextField");
        // Ascending ID
        util.typeString("sort:id");
        push(KeyCode.ENTER);
        doubleClick("#dummy/dummy_col0_filterTextField");
        // Descending ID (default)
        util.typeString("sort:-id");
        push(KeyCode.ENTER);
        doubleClick("#dummy/dummy_col0_filterTextField");
        // Comment count
        util.typeString("sort:comments");
        push(KeyCode.ENTER);
        doubleClick("#dummy/dummy_col0_filterTextField");
        // Repo ID (e.g. dummy2/dummy)
        util.typeString("sort:repo");
        push(KeyCode.ENTER);
        doubleClick("#dummy/dummy_col0_filterTextField");
        // Updated time (ascending)
        util.typeString("sort:updated");
        push(KeyCode.ENTER);
        doubleClick("#dummy/dummy_col0_filterTextField");
        // Updated time (descending)
        util.typeString("sort:-date");
        push(KeyCode.ENTER);
        doubleClick("#dummy/dummy_col0_filterTextField");
        // Issue state (open)
        util.typeString("sort:status");
        push(KeyCode.ENTER);
        doubleClick("#dummy/dummy_col0_filterTextField");
        // Issue state (closed)
        util.typeString("sort:-status");
        push(KeyCode.ENTER);
    }
}
