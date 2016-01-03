package guitests;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

import javafx.scene.input.KeyCode;
import org.junit.Test;
import ui.listpanel.ListPanel;

import java.util.Optional;

public class FilterTests extends UITest{

    @Test
    public void parseExceptionTest() {
        ListPanel issuePanel = find("#dummy/dummy_col0");

        // test parse exception returns Qualifier.EMPTY, i.e. all issues
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("milestone:");
        push(KeyCode.ENTER);

        assertEquals(10, issuePanel.getIssueCount());
    }

    @Test
    public void milestoneAliasFilterTest() {
        ListPanel issuePanel = find("#dummy/dummy_col0");

        // test current-1 : equal to first milestone in dummy repo
        checkCurrWithResult("milestone", "current-1", issuePanel, 1);

        // test current : equal to second milestone in dummy repo
        checkCurrWithResult("milestone", "current", issuePanel, 2);

        // test curr+1 : equal to third milestone in dummy repo
        checkCurrWithResult("milestone", "current+1", issuePanel, 3);

        // test current+2 : equal to fourth milestone in dummy repo
        checkCurrWithResult("milestone", "current+2", issuePanel, 4);

        // test current-2 : has no result
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("milestone:curr-2");
        push(KeyCode.ENTER);

        assertEquals(0, issuePanel.getIssueCount());

        // test current+3 : has no result
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("milestone:current+3");
        push(KeyCode.ENTER);

        assertEquals(0, issuePanel.getIssueCount());

        // test wrong alias
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("milestone:current+s0v8f");
        push(KeyCode.ENTER);
        assertEquals(0, issuePanel.getIssueCount());
    }

    @Test
    public void countFilterTest(){
        ListPanel issuePanel = find("#dummy/dummy_col0");

        // Checking 2 issues show for count:2
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("count:2");
        push(KeyCode.ENTER);

        assertEquals(2, issuePanel.getIssueCount());

        // Checking 7 issues shown for count:7
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("count:7");
        push(KeyCode.ENTER);

        assertEquals(7, issuePanel.getIssueCount());

        // if the count is greater than the number of issues, all the issues are shown in the list view
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("count:15");
        push(KeyCode.ENTER);

        assertEquals(10, issuePanel.getIssueCount());

        // Checking 0 issues show for count:0
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("count:0");
        push(KeyCode.ENTER);

        assertEquals(0, issuePanel.getIssueCount());

        //first count qualifier chosen
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("count:6 count:9");
        push(KeyCode.ENTER);

        assertEquals(6, issuePanel.getIssueCount());

        // Not-a-number returns the whole list

        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("count:abcd");
        push(KeyCode.ENTER);

        assertEquals(10, issuePanel.getIssueCount());

        // Not-a-number returns the whole list

        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("count:abcd");
        push(KeyCode.ENTER);

        assertEquals(10, issuePanel.getIssueCount());

        // Return the value from the first valid count qualifier

        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("count:abc count:8 count:2");
        push(KeyCode.ENTER);

        assertEquals(8, issuePanel.getIssueCount());
        assertEquals("Issue 10", issuePanel.getIssueList().get(0).getTitle());
        assertEquals("Issue 9", issuePanel.getIssueList().get(1).getTitle());

        // Test with sort qualifier

        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("count:8 count:2 sort:unmerged");
        push(KeyCode.ENTER);

        assertEquals(8, issuePanel.getIssueCount());
        assertEquals("Issue 1", issuePanel.getIssueList().get(0).getTitle());
        assertEquals("Issue 2", issuePanel.getIssueList().get(1).getTitle());
    }

    private void checkCurrWithResult(String milestoneAlias, String currString, ListPanel issuePanel,
                                     int milestoneNumber){
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type(milestoneAlias + ":" + currString);
        push(KeyCode.ENTER);

        assertEquals(1, issuePanel.getIssueCount());
        assertTrue(issuePanel.getIssueList().get(0).getMilestone().isPresent());
        assertEquals(Optional.of(milestoneNumber), issuePanel.getIssueList().get(0).getMilestone());
    }
}
