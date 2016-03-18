package guitests;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

import backend.stub.DummyRepoState;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.junit.Test;
import ui.listpanel.ListPanel;
import util.PlatformEx;

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
        PlatformEx.waitOnFxThread();

        assertEquals(DummyRepoState.noOfDummyIssues, issuePanel.getIssueCount());
    }

    @Test
    public void filterTextField_semanticException_emptyFilter() {
        ListPanel issuePanel = find("#dummy/dummy_col0");

        // test semantic exception dummy/dummy_col0_filterTextField");
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("id:buggy");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();

        assertEquals(DummyRepoState.noOfDummyIssues, issuePanel.getIssueCount());
    }

    @Test
    public void filterTextField_tallyMetadataUpdateSemanticException_emptyFilter() {
        ListPanel issuePanel = find("#dummy/dummy_col0");

        // test semantic exception dummy/dummy_col0_filterTextField");
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("id:buggy u:<24");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();

        assertEquals(DummyRepoState.noOfDummyIssues, issuePanel.getIssueCount());
    }

    @Test
    public void filterTextField_multiplePanels_correctPanelFiltered() {
        ListPanel issuePanel = find("#dummy/dummy_col0");

        // filter panel 1
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("id:4");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        assertEquals(1, issuePanel.getIssueCount());

        // create new panel 2
        pushKeys(new KeyCodeCombination(KeyCode.P, KeyCombination.SHORTCUT_DOWN));
        PlatformEx.waitOnFxThread();
        ListPanel issuePanel2 = find("#dummy/dummy_col1");

        // filter once
        click("#dummy/dummy_col1_filterTextField");
        type("id:3");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        assertEquals(1, issuePanel2.getIssueCount());

        // filter again and check if the correct panel is filtered (and the other panel is untouched)
        click("#dummy/dummy_col1_filterTextField");
        selectAll();
        push(KeyCode.BACK_SPACE);
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        assertEquals(DummyRepoState.noOfDummyIssues, issuePanel2.getIssueCount());
        assertEquals(1, issuePanel.getIssueCount());

        pushKeys(new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN));
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

        // Checking 7 issues shown for count:7
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("count:7");
        push(KeyCode.ENTER);

        assertEquals(7, issuePanel.getIssueCount());

        // Checking 10 issues shown for count:10
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("count:10");
        push(KeyCode.ENTER);

        assertEquals(10, issuePanel.getIssueCount());

        // Checking 0 issues show for count:0
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("count:0");
        push(KeyCode.ENTER);

        assertEquals(0, issuePanel.getIssueCount());

        // if the count is greater than the number of issues, all the issues are shown in the list view
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("count:15");
        push(KeyCode.ENTER);

        assertEquals(DummyRepoState.noOfDummyIssues, issuePanel.getIssueCount());

        //multiple count qualifiers
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("count:6 count:9");
        push(KeyCode.ENTER);

        assertEquals(DummyRepoState.noOfDummyIssues, issuePanel.getIssueCount());

        // Not-a-number

        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("count:abcd");
        push(KeyCode.ENTER);

        assertEquals(DummyRepoState.noOfDummyIssues, issuePanel.getIssueCount());

        // Test with sort qualifier as the second qualifier

        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("count:8 sort:unmerged");
        push(KeyCode.ENTER);

        assertEquals(8, issuePanel.getIssueCount());
        assertEquals("Issue 1", issuePanel.getElementsList().get(0).getIssue().getTitle());
        assertEquals("Issue 2", issuePanel.getElementsList().get(1).getIssue().getTitle());

        // Test with sort qualifier as the first qualifier
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("sort:unmerged count:8");
        push(KeyCode.ENTER);

        assertEquals(8, issuePanel.getIssueCount());
        assertEquals("Issue 1", issuePanel.getElementsList().get(0).getIssue().getTitle());
        assertEquals("Issue 2", issuePanel.getElementsList().get(1).getIssue().getTitle());

        // Checking for negative number
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type("count:-1");
        push(KeyCode.ENTER);

        assertEquals(DummyRepoState.noOfDummyIssues, issuePanel.getIssueCount());
    }

    private void checkCurrWithResult(String milestoneAlias, String currString, ListPanel issuePanel,
                                     int milestoneNumber){
        click("#dummy/dummy_col0_filterTextField");
        selectAll();
        type(milestoneAlias + ":" + currString);
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();

        assertEquals(1, issuePanel.getIssueCount());
        assertTrue(issuePanel.getElementsList().get(0).getIssue().getMilestone().isPresent());
        assertEquals(Optional.of(milestoneNumber), issuePanel.getElementsList().get(0).getIssue().getMilestone());
    }
}
