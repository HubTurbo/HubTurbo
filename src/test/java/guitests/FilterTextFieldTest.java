package guitests;

import static junit.framework.TestCase.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import javafx.scene.input.KeyCode;
import ui.TestController;
import ui.components.FilterTextField;
import ui.issuepanel.FilterPanel;

public class FilterTextFieldTest extends UITest {

    private void testCompletions(FilterTextField field) {
        // Basic completion
        clearField();
        type("cou").push(KeyCode.TAB);
        awaitCondition(() -> field.getText().equals("count"));

        // Completion does not only work for alternating keys typed
        clearField();
        type("c");
        awaitCondition(() -> field.getSelectedText().equals("losed"));
        type("l");
        awaitCondition(() -> field.getSelectedText().equals("osed"));
        type("o");
        awaitCondition(() -> field.getSelectedText().equals("sed"));
        type(KeyCode.TAB);
        awaitCondition(() -> field.getSelectedText().equals(""));
        awaitCondition(() -> field.getText().equals("closed"));

        // Completion with selection
        clearField();
        type("cou").push(KeyCode.TAB);
        push(KeyCode.LEFT);
        for (int i = 0; i < 3; i++) {
            field.selectBackward();
        }
        // c[oun]t
        type("lo").push(KeyCode.TAB); // 'c' + 'lo' is a prefix of 'closed'
        awaitCondition(() -> field.getText().equals("closedt"));
    }

    private void testSpaces(FilterTextField field) {
        // Consecutive spaces allowed
        clearField();
        type("cou").push(KeyCode.TAB);
        type("   ");
        awaitCondition(() -> field.getText().equals("count   "));

        // Insertion of spaces before spaces
        clearField();
        type("assi").push(KeyCode.TAB);
        type(" c").push(KeyCode.BACK_SPACE);
        push(KeyCode.LEFT, 2);
        type(" ");
        awaitCondition(() -> field.getText().equals("assignee  c"));

        // Insertion of spaces after spaces
        clearField();
        type("assi").push(KeyCode.TAB);
        type(" c").push(KeyCode.BACK_SPACE);
        push(KeyCode.LEFT);
        type(" ");
        awaitCondition(() -> field.getText().equals("assignee  c"));
    }

    @Test
    public void inputsTest() {
        FilterTextField field = getFirstPanelField();
        testCompletions(field);
        testSpaces(field);
    }

    @Test
    public void revertTest() {
        FilterTextField field = getFirstPanelField();

        type("assi").push(KeyCode.TAB);
        push(KeyCode.ENTER);

        clearField();
        type("test");
        awaitCondition(() -> field.getText().equals("test"));

        push(KeyCode.ESCAPE);

        awaitCondition(() -> field.getText().equals("assignee"));
    }

    @Test
    public void cancelTest() {
        FilterTextField field = getFirstPanelField();
        AtomicInteger toggle = new AtomicInteger(0);
        field.setOnCancel(toggle::getAndIncrement);

        assertTrue(toggle.get() % 2 == 0);

        type("mile").push(KeyCode.TAB);
        push(KeyCode.ENTER);

        clearField();
        type("test");
        awaitCondition(() -> field.getText().equals("test"));

        push(KeyCode.ESCAPE);
        awaitCondition(() -> toggle.get() % 2 == 0);

        push(KeyCode.ESCAPE);
        awaitCondition(() -> toggle.get() % 2 == 1);
    }

    private FilterTextField getFirstPanelField() {
        FilterPanel issuePanel = (FilterPanel) TestController.getUI().getPanelControl().getPanel(0);
        FilterTextField field = issuePanel.getFilterTextField();
        waitUntilNodeAppears(field);
        click(issuePanel.getFilterTextField());
        return field;
    }

    private void clearField() {
        selectAll();
        push(KeyCode.BACK_SPACE);
    }
}
