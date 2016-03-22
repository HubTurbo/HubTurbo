package guitests;

import static junit.framework.TestCase.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import javafx.scene.input.KeyCode;
import org.loadui.testfx.utils.FXTestUtils;
import ui.TestController;
import ui.UI;
import ui.components.FilterTextField;
import ui.issuepanel.FilterPanel;
import util.GitHubURL;
import util.events.testevents.NavigateToPageEventHandler;

public class FilterTextFieldTest extends UITest {

    @Override
    public void launchApp() {
        FXTestUtils.launchApp(
            TestUI.class, "--test=true", "--bypasslogin=true", "--testchromedriver=true");
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

        clearField();
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

    @Test
    public void showDocs_filterTextFieldInFocus_navigateToFilterDocsPage() {
        AtomicReference<String> url = new AtomicReference<>();
        UI.events.registerEvent((NavigateToPageEventHandler) e -> url.set(e.url));
        getFirstPanelField();
        push(KeyCode.F1);
        waitAndAssertEquals(GitHubURL.FILTERS_PAGE, url::get);
    }

    @Test
    public void filterTextFieldColor_validFilter_validFilterStyleApplied(){
        testValidFilterStyleApplied("is:open");
        testValidFilterStyleApplied("-is:open");
        testValidFilterStyleApplied("hello");
        testValidFilterStyleApplied("is:open & has:assignee");
        testValidFilterStyleApplied("(is:closed)");
    }

    @Test
    public void filterTextFieldColor_invalidFilter_invalidFilterStyleApplied(){

        // Tests if parse errors apply invalid filter style
        testInvalidFilterStyleApplied("is: is:");

        // Tests if semantic errors apply invalid filter style
        testInvalidFilterStyleAppliedAfterEnter("is:invalid");
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
        type(" c").push(KeyCode.BACK_SPACE); // cancel completion
        push(KeyCode.LEFT, 2);
        type(" ");
        awaitCondition(() -> field.getText().equals("assignee  c"));

        // Insertion of spaces after spaces
        clearField();
        type("assi").push(KeyCode.TAB);
        type(" c").push(KeyCode.BACK_SPACE); // cancel completion
        push(KeyCode.LEFT);
        type(" ");
        awaitCondition(() -> field.getText().equals("assignee  c"));

        // Insertion of spaces with trailing spaces
        clearField();
        type("assignee ");
        push(KeyCode.LEFT, 2);
        type(" ");
        push(KeyCode.LEFT, 8);
        type(" ");
        awaitCondition(() -> field.getText().equals(" assigne e "));
    }

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

    private FilterTextField getFirstPanelField() {
        FilterPanel issuePanel = (FilterPanel) TestController.getUI().getPanelControl().getPanel(0);
        FilterTextField field = issuePanel.getFilterTextField();
        waitUntilNodeAppears(field);
        click(issuePanel.getFilterTextField());
        return field;
    }

    private void testValidFilterStyleApplied(String filter) {
        FilterTextField field = getFirstPanelField();
        clearField();
        type(filter);
        assertTrue(field.getStyle().contains(FilterTextField.VALID_FILTER_STYLE));
    }

    private void testInvalidFilterStyleApplied(String filter) {
        FilterTextField field = getFirstPanelField();
        clearField();
        type(filter);
        assertTrue(field.getStyle().contains(FilterTextField.INVALID_FILTER_STYLE));
    }

    private void testInvalidFilterStyleAppliedAfterEnter(String filter) {
        FilterTextField field = getFirstPanelField();
        clearField();
        type(filter).push(KeyCode.ENTER);
        assertTrue(field.getStyle().contains(FilterTextField.INVALID_FILTER_STYLE));
    }

    private void clearField() {
        selectAll();
        push(KeyCode.BACK_SPACE);
    }
}
