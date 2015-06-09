package tests;

import backend.resource.TurboIssue;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.User;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class TurboIssueTest {

    @Test
    public void turboIssueTest() {
        Issue issue = new Issue();
        issue.setNumber(1);
        issue.setUser(new User().setLogin("test_user"));
        issue.setCreatedAt(new Date());
        issue.setUpdatedAt(new Date());
        issue.setState("open");
        issue.setLabels(new ArrayList<>());
        TurboIssue turboIssue = new TurboIssue("dummy/dummy", issue);
        assertEquals(1, turboIssue.getId());
    }

}
