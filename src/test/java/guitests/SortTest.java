package guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;

public class SortTest extends UITest {

    // TODO check that issue list is sorted correctly
    @Test
    public void sortTest() {
        clickFilterTextFieldAtPanel(0);
        // Ascending ID
        type("sort:id");
        push(KeyCode.ENTER);
        clickFilterTextFieldAtPanel(0);
        selectAll();
        // Descending ID (default)
        type("sort:-id");
        push(KeyCode.ENTER);
        clickFilterTextFieldAtPanel(0);
        selectAll();
        // Comment count
        type("sort:comments");
        push(KeyCode.ENTER);
        clickFilterTextFieldAtPanel(0);
        selectAll();
        // Repo ID (e.g. dummy2/dummy)
        type("sort:repo");
        push(KeyCode.ENTER);
        clickFilterTextFieldAtPanel(0);
        selectAll();
        // Updated time (ascending)
        type("sort:updated");
        push(KeyCode.ENTER);
        clickFilterTextFieldAtPanel(0);
        selectAll();
        // Updated time (descending)
        type("sort:-date");
        push(KeyCode.ENTER);
        clickFilterTextFieldAtPanel(0);
        selectAll();
        // Issue state (open)
        type("sort:status");
        push(KeyCode.ENTER);
        clickFilterTextFieldAtPanel(0);
        selectAll();
        // Issue state (closed)
        type("sort:-status");
        push(KeyCode.ENTER);
    }
}
