package guitests;

import static org.junit.Assert.assertEquals;

import backend.resource.TurboIssue;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import ui.listpanel.ListPanel;

import java.util.List;
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

        assertEquals(issuePanel.getIssueCount(), 10);

        // test parse exception returns Qualifier.EMPTY, i.e. all issues
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("m:");
        push(KeyCode.ENTER);

        assertEquals(issuePanel.getIssueCount(), 10);
    }

    @Test
    public void milestoneAliasFilterTest() {
        ListPanel issuePanel = find("#dummy/dummy_col0");

        // test current-1 : equal to first milestone in dummy repo
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("milestone:current-1");
        push(KeyCode.ENTER);

        assertEquals(issuePanel.getIssueCount(), 1);
        assertEquals(issuePanel.getIssueList().get(0).getMilestone().isPresent(), true);
        assertEquals(issuePanel.getIssueList().get(0).getMilestone(), Optional.of(1));

        // test current-1 : equal to first milestone in dummy repo using qualifier alias
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("m:current-1");
        push(KeyCode.ENTER);

        assertEquals(issuePanel.getIssueCount(), 1);
        assertEquals(issuePanel.getIssueList().get(0).getMilestone().isPresent(), true);
        assertEquals(issuePanel.getIssueList().get(0).getMilestone(), Optional.of(1));

        // test current : equal to second milestone in dummy repo
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("milestone:current");
        push(KeyCode.ENTER);

        assertEquals(issuePanel.getIssueCount(), 1);
        assertEquals(issuePanel.getIssueList().get(0).getMilestone().isPresent(), true);
        assertEquals(issuePanel.getIssueList().get(0).getMilestone(), Optional.of(2));

        // test current : equal to second milestone in dummy repo using qualifier alias
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("m:current");
        push(KeyCode.ENTER);

        assertEquals(issuePanel.getIssueCount(), 1);
        assertEquals(issuePanel.getIssueList().get(0).getMilestone().isPresent(), true);
        assertEquals(issuePanel.getIssueList().get(0).getMilestone(), Optional.of(2));

        // test curr+1 : equal to third milestone in dummy repo
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("milestone:curr+1");
        push(KeyCode.ENTER);

        assertEquals(issuePanel.getIssueCount(), 1);
        assertEquals(issuePanel.getIssueList().get(0).getMilestone().isPresent(), true);
        assertEquals(issuePanel.getIssueList().get(0).getMilestone(), Optional.of(3));

        //testing curr + 1 with qualifer alias
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("m:curr+1");
        push(KeyCode.ENTER);

        assertEquals(issuePanel.getIssueCount(), 1);
        assertEquals(issuePanel.getIssueList().get(0).getMilestone().isPresent(), true);
        assertEquals(issuePanel.getIssueList().get(0).getMilestone(), Optional.of(3));

        // test current+2 : equal to fourth milestone in dummy repo
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("milestone:current+2");
        push(KeyCode.ENTER);

        assertEquals(issuePanel.getIssueCount(), 1);
        assertEquals(issuePanel.getIssueList().get(0).getMilestone().isPresent(), true);
        assertEquals(issuePanel.getIssueList().get(0).getMilestone(), Optional.of(4));

        // test current-2 : has no result
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("milestone:curr-2");
        push(KeyCode.ENTER);

        assertEquals(issuePanel.getIssueCount(), 0);

        // test current+3 : has no result
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("milestone:current+3");
        push(KeyCode.ENTER);

        assertEquals(issuePanel.getIssueCount(), 0);

        // test wrong alias
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("milestone:current+s0v8f");
        push(KeyCode.ENTER);
        assertEquals(issuePanel.getIssueCount(), 0);
    }
}
