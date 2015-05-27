package tests;

import backend.stub.DummyRepo;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class DummyRepoTest {

    @Test
    public void testUpdates() {
        String dummyId = "dummy/dummy";
        DummyRepo dummy = new DummyRepo();

        for (int i = 0; i < 100; i++) {
            dummy.getUpdatedIssues(dummyId, "", new Date());
            dummy.getUpdatedLabels(dummyId, "");
            dummy.getUpdatedMilestones(dummyId, "");
            dummy.getUpdatedCollaborators(dummyId, "");
        }

        assertEquals(110, dummy.getIssues(dummyId).size());
        assertEquals(110, dummy.getLabels(dummyId).size());
        assertEquals(110, dummy.getMilestones(dummyId).size());
        assertEquals(110, dummy.getCollaborators(dummyId).size());
    }
}
