package guitests;

import static junit.framework.TestCase.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
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
    public void completion_validPrefixes_match() {
        FilterTextField field = getFirstPanelField();

        // Basic completion
        clearField();
        type("cou").push(KeyCode.TAB);
        waitAndAssertEquals("count", field::getText);

        // Completion with selection
        clearField();
        type("cou").push(KeyCode.TAB);
        push(KeyCode.LEFT);
        for (int i = 0; i < 3; i++) {
            field.selectBackward();
        }
        // c[oun]t
        type("lo").push(KeyCode.TAB); // 'c' + 'lo' is a prefix of 'closed'
        waitAndAssertEquals("closedt", field::getText);
    }

    @Test
    public void completion_isNavigatingAndEnter_match() {
        FilterTextField field = getFirstPanelField();

        clearField();
        type("a");
        push(KeyCode.DOWN, 4);
        press(KeyCode.ENTER);
        waitAndAssertEquals("creator", field::getText);
    }

    @Test
    public void completion_escapeOnce_filterRemainsUnchanged() {
        FilterTextField field = getFirstPanelField();

        clearField();
        type("a");
        press(KeyCode.ESCAPE);
        waitAndAssertEquals("a", field::getText);
    }

    @Test
    public void inputHandleSpaces_fieldNotEmpty_allowSpaces() {
        FilterTextField field = getFirstPanelField();
        // Consecutive spaces allowed
        clearField();
        type("cou").push(KeyCode.TAB);
        type("   ");
        waitAndAssertEquals("count   ", field::getText);

        // Insertion of spaces before spaces
        clearField();
        type("assi").push(KeyCode.TAB);
        type(" c").push(KeyCode.BACK_SPACE); // cancel completion
        push(KeyCode.LEFT);
        type(" ");
        waitAndAssertEquals("assignee  ", field::getText);

        // Insertion of spaces with trailing spaces
        clearField();
        type("assignee ");
        push(KeyCode.LEFT, 2);
        type(" ");
        push(KeyCode.LEFT, 8);
        type(" ");
        waitAndAssertEquals(" assigne e ", field::getText);
    }

    @Test
    public void revertTextEdit_overwritePrevFilterText_revertPrev() {
        FilterTextField field = getFirstPanelField();

        type("assi").push(KeyCode.TAB);
        push(KeyCode.ENTER);

        clearField();
        type("test");
        waitAndAssertEquals("test", field::getText);

        push(KeyCode.ESCAPE);
        waitAndAssertEquals("assignee", field::getText);
    }

    @Test
    public void detectCancelEvent_noMoreReverts_detected() {
        FilterTextField field = getFirstPanelField();
        AtomicInteger toggle = new AtomicInteger(0);
        field.setOnCancel(toggle::getAndIncrement);

        assertTrue(toggle.get() % 2 == 0);

        type("mile").push(KeyCode.TAB);
        push(KeyCode.ENTER);

        clearField();
        type("test");
        waitAndAssertEquals("test", field::getText);

        push(KeyCode.ESCAPE);
        waitAndAssertEquals(0, () -> toggle.get() % 2);

        push(KeyCode.ESCAPE);
        waitAndAssertEquals(1, () -> toggle.get() % 2);
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
    public void filterTextFieldColor_validFilter_validFilterStyleApplied() {
        testValidFilterStyleApplied("is:open");
        testValidFilterStyleApplied("-is:open");
        testValidFilterStyleApplied("hello");
        testValidFilterStyleApplied("is:open & has:assignee");
        testValidFilterStyleApplied("(is:closed)");
    }

    @Test
    public void filterTextFieldColor_invalidFilter_invalidFilterStyleApplied() {

        // Tests if parse errors apply invalid filter style
        testInvalidFilterStyleApplied("is: is:");

        // Tests if semantic errors apply invalid filter style
        testInvalidFilterStyleAppliedAfterEnter("is:invalid");
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

    @After
    public void clearField() {
        selectAll();
        push(KeyCode.BACK_SPACE);
    }
}
