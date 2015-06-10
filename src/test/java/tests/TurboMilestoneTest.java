package tests;

import backend.resource.TurboMilestone;
import org.eclipse.egit.github.core.Milestone;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class TurboMilestoneTest {

    @Test
    public void turboMilestoneTest() {
        Milestone milestone = new Milestone();
        milestone.setNumber(1);
        milestone.setState("open");
        TurboMilestone turboMilestone = new TurboMilestone("dummy/dummy", milestone);
        assertEquals(1, turboMilestone.getId());
        assertEquals("dummy/dummy", turboMilestone.getRepoId());
        turboMilestone.setDueDate(Optional.<LocalDate>empty());
        assertEquals(Optional.empty(), turboMilestone.getDueDate());
        turboMilestone.setDescription("test description");
        assertEquals("test description", turboMilestone.getDescription());
        turboMilestone.setOpen(false);
        assertEquals(false, turboMilestone.isOpen());
        turboMilestone.setOpenIssues(0);
        assertEquals(0, turboMilestone.getOpenIssues());
        turboMilestone.setClosedIssues(0);
        assertEquals(0, turboMilestone.getClosedIssues());
    }

}
