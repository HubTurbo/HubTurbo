package tests;

import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.User;
import org.junit.Test;
import util.Utility;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.Assert.*;

public class TurboIssueTests {

    private static final String REPO = "testrepo/testrepo";

    private TurboIssue createIssueWithUpdatedAt(int number, LocalDateTime updatedAt) {
        TurboIssue issue = new TurboIssue(REPO, number, "", "", null, false);
        issue.setUpdatedAt(updatedAt);

        return issue;
    }

    private PullRequest createPullRequestWithUpdatedAt(int number, LocalDateTime updatedAt) {
        Date date = Utility.localDateTimeToDate(updatedAt);

        PullRequest pullRequest = new PullRequest();
        pullRequest.setNumber(number);
        pullRequest.setUpdatedAt(date);

        return pullRequest;
    }

    @Test
    public void turboIssueTest() {
        Issue issue = new Issue();
        issue.setNumber(1);
        issue.setUser(new User().setLogin("test_user"));
        issue.setCreatedAt(new Date());
        issue.setUpdatedAt(new Date());
        issue.setState("open");
        ArrayList<Label> labels = new ArrayList<>();
        labels.add(new Label().setName("test label"));
        issue.setLabels(labels);
        TurboIssue turboIssue = new TurboIssue("dummy/dummy", issue);
        assertEquals(1, turboIssue.getId());
        assertEquals("test_user", turboIssue.getCreator());
        assertEquals(true, turboIssue.isOpen());
        assertEquals("test label", turboIssue.getLabels().get(0));
    }

    /**
     * Tests TurboIssue's isCurrentRead method
     */
    @Test
    public void testReadState() {
        TurboIssue issue = new TurboIssue(REPO, 1, "", "", null, false);

        // An issue is not read if it doesn't record any markedReadAt time
        assertFalse(issue.isCurrentlyRead());

        // An issue is not read if its markedAsRead time is before its updatedAt time
        issue.setUpdatedAt(LocalDateTime.of(2015, 2, 17, 2, 10));
        issue.setMarkedReadAt(Optional.of(LocalDateTime.of(2015, 1, 6, 12, 15)));
        assertFalse(issue.isCurrentlyRead());

        // An issue is marked as read if its markedAsRead time is after its updated Time
        issue.setUpdatedAt(LocalDateTime.of(2015, 1, 1, 1, 1));
        issue.setMarkedReadAt(Optional.of(LocalDateTime.of(2015, 1, 6, 12, 15)));
        assertTrue(issue.isCurrentlyRead());
    }

    @Test
    public void testCombinePullRequest() {
        TurboIssue issue1 = createIssueWithUpdatedAt(1, LocalDateTime.of(2015, 2, 17, 2, 10));
        TurboIssue issue2 = createIssueWithUpdatedAt(2, LocalDateTime.of(2015, 2, 18, 2, 10));
        TurboIssue issue3 = createIssueWithUpdatedAt(3, LocalDateTime.of(2015, 2, 19, 2, 10));
        TurboIssue issue4 = createIssueWithUpdatedAt(4, LocalDateTime.of(2015, 2, 20, 2, 10));

        List<TurboIssue> issues = new ArrayList<>();
        issues.add(issue1);
        issues.add(issue2);
        issues.add(issue3);
        issues.add(issue4);
        Collections.shuffle(issues);

        PullRequest pr1 = createPullRequestWithUpdatedAt(1, LocalDateTime.of(2015, 7, 7, 1, 21));
        PullRequest pr2 = createPullRequestWithUpdatedAt(2, LocalDateTime.of(2015, 2, 18, 2, 9));
        PullRequest pr3 = new PullRequest();
        pr3.setNumber(3);
        PullRequest pr5 = new PullRequest();
        pr5.setNumber(5);

        List<PullRequest> pullRequests = new ArrayList<>();
        pullRequests.add(pr1);
        pullRequests.add(pr2);
        pullRequests.add(pr3);
        pullRequests.add(pr5);
        Collections.shuffle(pullRequests);

        List<TurboIssue> newIssues = TurboIssue.combineWithPullRequests(issues, pullRequests);
        TurboIssue newIssue1 = newIssues.get(TurboIssue.findIssueWithId(newIssues, 1).get());
        TurboIssue newIssue2 = newIssues.get(TurboIssue.findIssueWithId(newIssues, 2).get());
        TurboIssue newIssue3 = newIssues.get(TurboIssue.findIssueWithId(newIssues, 3).get());
        TurboIssue newIssue4 = newIssues.get(TurboIssue.findIssueWithId(newIssues, 4).get());
        assertEquals(LocalDateTime.of(2015, 7, 7, 1, 21), newIssue1.getUpdatedAt());
        assertEquals(issue2.getUpdatedAt(), newIssue2.getUpdatedAt());
        assertEquals(issue3.getUpdatedAt(), newIssue3.getUpdatedAt());
        assertEquals(issue4.getUpdatedAt(), newIssue4.getUpdatedAt());
    }

    /**
     * Tests that when an issue is created and no manual labels change have been made, the labels
     * modified time is equal to the issue's updatedAt time
     */
    @Test
    public void getLabelsModifiedAt_initial() {
        TurboIssue issue = new TurboIssue("testrepo", 1, "Issue title");
        assertEquals(issue.getUpdatedAt(), issue.getLabelsLastModifiedAt());
    }

    /**
     * Tests that labels modified time is updated properly when methods mutating the issue' labels is called
     */
    @Test
    public void getLabelsModifiedAt_updated() throws InterruptedException {
        TurboIssue issue = new TurboIssue("testrepo", 1, "Issue title");

        LocalDateTime checkPoint = issue.getLabelsLastModifiedAt();
        TestUtils.delayThenRun(10, () -> issue.setLabels(new ArrayList<>()));
        assertTrue(issue.getLabelsLastModifiedAt().isAfter(checkPoint));

        checkPoint = issue.getLabelsLastModifiedAt();
        TestUtils.delayThenRun(10, () -> issue.addLabel("label"));
        assertTrue(issue.getLabelsLastModifiedAt().isAfter(checkPoint));

        checkPoint = issue.getLabelsLastModifiedAt();
        TestUtils.delayThenRun(10, () -> issue.addLabel(new TurboLabel("testrepo", "label")));
        assertTrue(issue.getLabelsLastModifiedAt().isAfter(checkPoint));
    }

    /**
     * Tests that when an issue is created and no manual state change have been made, the state
     * modified time is equal to the issue's updatedAt time
     */
    @Test
    public void getStateModifiedAt_initial() {
        TurboIssue issue = new TurboIssue("testrepo", 1, "Issue title");
        assertEquals(issue.getUpdatedAt(), issue.getStateLastModifiedAt());
    }

    /**
     * Tests that state modified time is updated properly when {@code setOpen} is called
     */
    @Test
    public void getStateModifiedAt_updated() throws InterruptedException {
        TurboIssue issue = new TurboIssue("testrepo", 1, "Issue title");

        LocalDateTime checkPoint = issue.getStateLastModifiedAt();
        TestUtils.delayThenRun(10, () -> issue.setOpen(false));
        assertTrue(issue.getStateLastModifiedAt().isAfter(checkPoint));
    }

    /**
     * Tests that if the updated issue's labels are more recently modified,
     * it overrides the original issue's labels
     */
    @Test
    public void reconcile_moreRecentlyUpdated_Override() throws InterruptedException {
        TurboIssue originalIssue = LogicTests.createIssueWithLabels(1, new ArrayList<>());
        List<String> newLabels = Arrays.asList("label1", "label2");
        TurboIssue updatedIssue = TestUtils.delayThenGet(
                10, () -> LogicTests.createIssueWithLabels(1, newLabels));

        List<TurboIssue> updatedList = TurboIssue.reconcile(Arrays.asList(originalIssue),
                                                            Arrays.asList(updatedIssue));
        assertEquals(newLabels, updatedList.get(0).getLabels());
    }

    /**
     * Tests that if the original issue's labels are retained if it's more recently modified
     * then the updated issue's labels
     */
    @Test
    public void reconcile_stale_retained() throws InterruptedException {
        TurboIssue updatedIssue = LogicTests.createIssueWithLabels(1, new ArrayList<>());
        List<String> originalLabels = Arrays.asList("label1", "label2");
        TurboIssue originalIssue = TestUtils.delayThenGet(
                10, () -> LogicTests.createIssueWithLabels(1, originalLabels));

        List<TurboIssue> updatedList = TurboIssue.reconcile(Arrays.asList(originalIssue),
                                                            Arrays.asList(updatedIssue));
        assertEquals(originalLabels, updatedList.get(0).getLabels());
    }
}
