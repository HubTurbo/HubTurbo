package guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;
import ui.IdGenerator;

public class SortTest extends UITest {

    // TODO check that issue list is sorted correctly
    @Test
    public void sortTest() {
        String filterTextFieldId = IdGenerator.getPanelFilterTextFieldIdForTest("dummy/dummy", 0);
        click(filterTextFieldId);
        // Ascending ID
        type("sort:id");
        push(KeyCode.ENTER);
        click(filterTextFieldId);
        selectAll();
        // Descending ID (default)
        type("sort:-id");
        push(KeyCode.ENTER);
        click(filterTextFieldId);
        selectAll();
        // Comment count
        type("sort:comments");
        push(KeyCode.ENTER);
        click(filterTextFieldId);
        selectAll();
        // Repo ID (e.g. dummy2/dummy)
        type("sort:repo");
        push(KeyCode.ENTER);
        click(filterTextFieldId);
        selectAll();
        // Updated time (ascending)
        type("sort:updated");
        push(KeyCode.ENTER);
        click(filterTextFieldId);
        selectAll();
        // Updated time (descending)
        type("sort:-date");
        push(KeyCode.ENTER);
        click(filterTextFieldId);
        selectAll();
        // Issue state (open)
        type("sort:status");
        push(KeyCode.ENTER);
        click(filterTextFieldId);
        selectAll();
        // Issue state (closed)
        type("sort:-status");
        push(KeyCode.ENTER);
    }
}
