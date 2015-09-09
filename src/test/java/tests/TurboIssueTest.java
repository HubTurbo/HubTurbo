package tests;

import backend.resource.TurboIssue;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.User;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TurboIssueTest {
    private static String REPO = "testrepo/testrepo";

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
        TurboIssue issue = new TurboIssue(REPO, 1, "", "",
                LocalDateTime.of(2011, 1, 1, 1, 1, 1), false);

        // An issue is not read if it doesn't record any markedReadAt time
        assertFalse(issue.isCurrentlyRead());

        // An issue is read if it has no updatedAt time
        // and its markedAsRead time is after its createdAt time
        issue.setMarkedReadAt(Optional.of(LocalDateTime.of(2015, 1, 6, 12, 15)));
        issue.setUpdatedAt(null);
        assertTrue(issue.isCurrentlyRead());

        // An issue is not read if its markedAsRead time is before its updatedAt time
        issue.setUpdatedAt(LocalDateTime.of(2015, 2, 17, 2, 10));
        issue.setMarkedReadAt(Optional.of(LocalDateTime.of(2015, 1, 6, 12, 15)));
        assertFalse(issue.isCurrentlyRead());

        // An issue is marked as read if its markedAsRead time is after its updated Time
        issue.setMarkedReadAt(Optional.of(LocalDateTime.of(2015, 3, 6, 12, 15)));
        assertTrue(issue.isCurrentlyRead());
    }

}
