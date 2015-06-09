package tests;

import backend.resource.TurboIssue;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
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
        ArrayList<Label> labels = new ArrayList<>();
        labels.add(new Label().setName("test label"));
        issue.setLabels(labels);
        TurboIssue turboIssue = new TurboIssue("dummy/dummy", issue);
        assertEquals(1, turboIssue.getId());
        assertEquals("test_user", turboIssue.getCreator());
        assertEquals(true, turboIssue.isOpen());
        assertEquals("test label", turboIssue.getLabels().get(0));
    }

}
