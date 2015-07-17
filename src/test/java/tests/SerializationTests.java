package tests;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.User;
import org.junit.Before;
import org.junit.Test;

import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import backend.resource.TurboMilestone;
import backend.resource.TurboUser;
import backend.resource.serialization.SerializableIssue;
import backend.resource.serialization.SerializableLabel;
import backend.resource.serialization.SerializableMilestone;
import backend.resource.serialization.SerializableUser;

public class SerializationTests {

    private static final String TEST_ISSUE_STRING_FORMMATER =
              "Issue: {%n"
            + "  id: 1,%n"
            + "  title: test title,%n"
            + "  creator: test_user,%n"
            + "  createdAt: 1991-06-01T02:03:04,%n"
            + "  isPullRequest: false,%n"
            + "  description: test description,%n"
            + "  updatedAt: 1991-06-02T04:03:02,%n"
            + "  commentCount: 0,%n"
            + "  isOpen: true,%n"
            + "  assignee: %s,%n"
            + "  labels: [test label0, test label1],%n"
            + "  milestone: %s,%n"
            + "}";

    private Issue testIssue;

    @Before
    public void setUp() {
        Date createdDate = (new GregorianCalendar(1991, 5, 1, 2, 3, 4)).getTime();
        Date updatedDate = (new GregorianCalendar(1991, 5, 2, 4, 3, 2)).getTime();

        ArrayList<Label> labels = new ArrayList<>();
        labels.add(new Label().setName("test label0"));
        labels.add(new Label().setName("test label1"));

        testIssue = new Issue();
        testIssue.setNumber(1);
        testIssue.setUser(new User().setLogin("test_user"));
        testIssue.setCreatedAt(createdDate);
        testIssue.setUpdatedAt(updatedDate);
        testIssue.setState("open");
        testIssue.setLabels(labels);
        testIssue.setTitle("test title");
    }


    @Test
    public void testSerializableLabelNoColorColorToString() {
        TurboLabel label = new TurboLabel("dummy/dummy", "label.name");
        SerializableLabel serializedLabel = new SerializableLabel(label);

        assertEquals("Label: {name: label.name, color: ffffff}",
                     serializedLabel.toString());
    }

    @Test
    public void testSerializableLabelWithColorToString() {
        TurboLabel label = new TurboLabel("dummy/dummy", "abcdef", "label.name");
        SerializableLabel serializedLabel = new SerializableLabel(label);

        assertEquals("Label: {name: label.name, color: abcdef}",
                     serializedLabel.toString());
    }

    @Test
    public void testSerializableMilestoneNoDueDateToString() {
        Milestone milestone = new Milestone();
        milestone.setNumber(1);
        milestone.setTitle("test milestone");
        milestone.setState("open");

        TurboMilestone turboMilestone = new TurboMilestone("dummy/dummy", milestone);
        turboMilestone.setDueDate(Optional.<LocalDate>empty());
        turboMilestone.setDescription("test description");
        turboMilestone.setOpen(false);
        turboMilestone.setOpenIssues(0);
        turboMilestone.setClosedIssues(0);

        SerializableMilestone serializedMilestone = new SerializableMilestone(turboMilestone);

        String formatter = "Milestone: {%n"
                + "  id: 1,%n"
                + "  title: test milestone,%n"
                + "  dueDate: ,%n"
                + "  description: test description,%n"
                + "  isOpen: false,%n"
                + "  openIssues: 0,%n"
                + "  closedIssues: 0,%n"
                + "}";

        assertEquals(String.format(formatter), serializedMilestone.toString());
    }

    @Test
    public void testSerializableMilestoneWithDueDateToString() {
        Milestone milestone = new Milestone();
        milestone.setNumber(1);
        milestone.setTitle("test milestone");
        milestone.setState("open");

        TurboMilestone turboMilestone = new TurboMilestone("dummy/dummy", milestone);
        turboMilestone.setDueDate(Optional.of(LocalDate.of(1991, 1, 1)));
        turboMilestone.setDescription("test description");
        turboMilestone.setOpen(true);
        turboMilestone.setOpenIssues(0);
        turboMilestone.setClosedIssues(0);

        SerializableMilestone serializedMilestone = new SerializableMilestone(turboMilestone);

        String formatter = "Milestone: {%n"
                + "  id: 1,%n"
                + "  title: test milestone,%n"
                + "  dueDate: 1991-01-01,%n"
                + "  description: test description,%n"
                + "  isOpen: true,%n"
                + "  openIssues: 0,%n"
                + "  closedIssues: 0,%n"
                + "}";

        assertEquals(String.format(formatter), serializedMilestone.toString());
    }

    @Test
    public void testSerializableUserToString() {
        TurboUser user = new TurboUser("dummy/dummy", "alice123", "Alice");
        String expectedString = "User: {loginName: alice123, realName: Alice, avatarURL: }";

        SerializableUser serializedUser = new SerializableUser(user);

        assertEquals(expectedString, serializedUser.toString());
    }

    @Test
    public void testSerializableIssueToString() {
        TurboIssue turboIssue = new TurboIssue("dummy/dummy", testIssue);
        turboIssue.setDescription("test description");
        SerializableIssue serializedIssue = new SerializableIssue(turboIssue);

        // Issue has no assignee and milestone
        assertEquals(String.format(TEST_ISSUE_STRING_FORMMATER, "", ""),
                     serializedIssue.toString());

        turboIssue.setAssignee("test assignee");
        turboIssue.setMilestone(1);
        serializedIssue = new SerializableIssue(turboIssue);

        // Issue has assignee and milestone
        assertEquals(String.format(TEST_ISSUE_STRING_FORMMATER,
                                   "test assignee", "1"),
                     serializedIssue.toString());
    }
}
