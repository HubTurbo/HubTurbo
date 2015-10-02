package tests;

import backend.resource.TurboIssue;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.User;
import org.junit.Test;
import util.Utility;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.Assert.*;

public class TurboIssueTest {

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

        List<PullRequest> pullRequests = new ArrayList<>();
        pullRequests.add(pr1);
        pullRequests.add(pr2);
        pullRequests.add(pr3);
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
}
