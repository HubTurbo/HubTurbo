package tests;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.Optional;

import org.eclipse.egit.github.core.Milestone;
import org.junit.Test;

import backend.resource.TurboLabel;
import backend.resource.TurboMilestone;
import backend.resource.TurboUser;
import backend.resource.serialization.SerializableLabel;
import backend.resource.serialization.SerializableMilestone;
import backend.resource.serialization.SerializableUser;

public class SerializationTests {

    @Test
    public void testSerializableLabelNoColorColorToString() {
        TurboLabel label = new TurboLabel("dummy/dummy", "label.name");
        SerializableLabel serializedLabel = new SerializableLabel(label);

        assertEquals(serializedLabel.toString(), "Label: {name: label.name, color: ffffff}");
    }

    @Test
    public void testSerializableLabelWithColorToString() {
        TurboLabel label = new TurboLabel("dummy/dummy", "abcdef", "label.name");
        SerializableLabel serializedLabel = new SerializableLabel(label);

        assertEquals(serializedLabel.toString(), "Label: {name: label.name, color: abcdef}");
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

        assertEquals(serializedMilestone.toString(), String.format(formatter));
    }

    @Test
    public void testSerializableMilestoneWithDueDateToString() {
        Milestone milestone = new Milestone();
        milestone.setNumber(1);
        milestone.setTitle("test milestone");
        milestone.setState("open");

        TurboMilestone turboMilestone = new TurboMilestone("dummy/dummy", milestone);
        turboMilestone.setDueDate(Optional.of(LocalDate.of(1, 1, 1)));
        turboMilestone.setDescription("test description");
        turboMilestone.setOpen(true);
        turboMilestone.setOpenIssues(0);
        turboMilestone.setClosedIssues(0);

        SerializableMilestone serializedMilestone = new SerializableMilestone(turboMilestone);

        String formatter = "Milestone: {%n"
                + "  id: 1,%n"
                + "  title: test milestone,%n"
                + "  dueDate: 0001-01-01,%n"
                + "  description: test description,%n"
                + "  isOpen: true,%n"
                + "  openIssues: 0,%n"
                + "  closedIssues: 0,%n"
                + "}";

        assertEquals(serializedMilestone.toString(), String.format(formatter));
    }

    @Test
    public void testSerializableUserToString() {
        TurboUser user = new TurboUser("dummy/dummy", "alice123", "Alice");
        String expectedString = "User: {loginName: alice123, realName: Alice, avatarURL: }";

        SerializableUser serializedUser = new SerializableUser(user);

        assertEquals(expectedString, serializedUser.toString());
    }
}
