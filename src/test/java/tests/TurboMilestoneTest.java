package tests;

import backend.resource.TurboMilestone;
import org.eclipse.egit.github.core.Milestone;
import org.junit.Test;

import java.time.LocalDate;
import java.util.*;

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

    @Test
    public void getDueDateComparator_combinationOfDifferentMilestones_correctMilestoneOrdering() {
        // expects : closed milestones without due date - milestones with due date ordered by due date - open
        // milestone without due date
        TurboMilestone milestoneWithDueDateClosed1 = new TurboMilestone("1", 1, "milestone01");
        milestoneWithDueDateClosed1.setDueDate(Optional.of(LocalDate.now().plusDays(5)));
        milestoneWithDueDateClosed1.setOpen(false);
        TurboMilestone milestoneWithDueDateClosed2 = new TurboMilestone("2", 2, "milestone02");
        milestoneWithDueDateClosed2.setDueDate(Optional.of(LocalDate.now().plusDays(3)));
        milestoneWithDueDateClosed2.setOpen(false);

        TurboMilestone milestoneWithDueDateOpen1 = new TurboMilestone("1", 11, "milestone21");
        milestoneWithDueDateOpen1.setDueDate(Optional.of(LocalDate.now().plusDays(2)));
        milestoneWithDueDateOpen1.setOpen(true);
        TurboMilestone milestoneWithDueDateOpen2 = new TurboMilestone("1", 12, "milestone22");
        milestoneWithDueDateOpen2.setDueDate(Optional.of(LocalDate.now().plusDays(4)));
        milestoneWithDueDateOpen2.setOpen(true);

        TurboMilestone milestoneWithoutDueDateOpen1 = new TurboMilestone("1", 21, "milestone31");
        TurboMilestone milestoneWithoutDueDateOpen2 = new TurboMilestone("2", 21, "milestone32");

        TurboMilestone milestoneWithoutDueDateClosed1 = new TurboMilestone("1", 31, "milestone11");
        milestoneWithoutDueDateClosed1.setOpen(false);
        TurboMilestone milestoneWithoutDueDateClosed2 = new TurboMilestone("1", 32, "milestone12");
        milestoneWithoutDueDateClosed2.setOpen(false);

        Comparator<TurboMilestone> comparator = TurboMilestone.getDueDateComparator();
        List<TurboMilestone> milestones = Arrays.asList(milestoneWithDueDateOpen1, milestoneWithDueDateOpen2,
                                                        milestoneWithDueDateClosed1, milestoneWithDueDateClosed2,
                                                        milestoneWithoutDueDateOpen1, milestoneWithoutDueDateOpen2,
                                                        milestoneWithoutDueDateClosed1, milestoneWithoutDueDateClosed2);
        Collections.sort(milestones, comparator);
        List<TurboMilestone> expected = Arrays.asList(milestoneWithoutDueDateClosed1, milestoneWithoutDueDateClosed2,
                                                      milestoneWithDueDateOpen1, milestoneWithDueDateClosed2,
                                                      milestoneWithDueDateOpen2, milestoneWithDueDateClosed1,
                                                      milestoneWithoutDueDateOpen1, milestoneWithoutDueDateOpen2);
        assertEquals(expected, milestones);
    }

    @Test
    public void sortByDueDate_sameMilestoneDueDate_stableSorting() {
        // test for turbo milestone due date sorting behaviour
        TurboMilestone milestone1 = new TurboMilestone("1", 1, "milestone1");
        milestone1.setDueDate(Optional.of(LocalDate.now().minusDays(1)));
        TurboMilestone milestone2 = new TurboMilestone("2", 2, "milestone2");
        milestone2.setDueDate(Optional.of(LocalDate.now().minusDays(2)));
        TurboMilestone milestone3 = new TurboMilestone("3", 3, "milestone3");
        milestone3.setDueDate(Optional.of(LocalDate.now().minusDays(1)));
        TurboMilestone milestone4 = new TurboMilestone("3", 4, "milestone4");
        TurboMilestone milestone5 = new TurboMilestone("3", 5, "milestone5");

        // the sort should be stable
        List<TurboMilestone> milestones = Arrays.asList(milestone1,
                                                        milestone2,
                                                        milestone3,
                                                        milestone4,
                                                        milestone5);
        List<TurboMilestone> sortedMilestones = TurboMilestone.sortByDueDate(milestones);
        assertEquals("milestone2", sortedMilestones.get(0).getTitle());
        assertEquals("milestone1", sortedMilestones.get(1).getTitle());
        assertEquals("milestone3", sortedMilestones.get(2).getTitle());
        assertEquals("milestone4", sortedMilestones.get(3).getTitle());
        assertEquals("milestone5", sortedMilestones.get(4).getTitle());
    }

}
