//package tests;
//
//import backend.UpdateSignature;
//import backend.resource.*;
//import backend.resource.serialization.SerializableModel;
//import backend.stub.DummyRepo;
//import org.junit.Test;
//
//import static org.junit.Assert.assertEquals;
//
//import java.util.Optional;
//
//public class ModelTests {
//    String repoId = "dummy/dummy";
//    DummyRepo dummy1 = new DummyRepo();
//    DummyRepo dummy2 = new DummyRepo();
//
//    // Model with empty signature
//    Model model1 = new Model(repoId,
//            dummy1.getIssues(repoId),
//            dummy1.getLabels(repoId),
//            dummy1.getMilestones(repoId),
//            dummy1.getCollaborators(repoId));
//    // Also empty signature, but explicitly declared
//    Model model2 = new Model(repoId,
//            dummy2.getIssues(repoId),
//            dummy2.getLabels(repoId),
//            dummy2.getMilestones(repoId),
//            dummy2.getCollaborators(repoId),
//            UpdateSignature.empty);
//    // Actually empty model
//    Model emptyModel = new Model(repoId, UpdateSignature.empty);
//    // Model to test for copy correctness
//    Model copiedModel = new Model(model2);
//
//    @Test
//    public void constructors() {
//        // Test correctness of first two constructors
//        assertEquals(model1, model2);
//
//        // Test for copy correctness
//        // TODO test if deep copy
//        assertEquals(model1, copiedModel);
//        assertEquals(model2, copiedModel);
//
//        // Test for conversion to and from SerializableModel
//        SerializableModel serializedModel = new SerializableModel(model1);
//        Model deserializedModel = new Model(serializedModel);
//
//        // Test for serialization correctness
//        assertEquals(model1, deserializedModel);
//    }
//
//    @Test
//    public void gets() {
//        // ID
//        assertEquals("dummy/dummy", model1.getRepoId());
//        assertEquals(model1.getRepoId(), model2.getRepoId());
//        // Signature
//        assertEquals(model1.getUpdateSignature(), UpdateSignature.empty);
//        assertEquals(model2.getUpdateSignature(), UpdateSignature.empty);
//        // Issues
//        int issueCount = 1;
//        for (TurboIssue issue : model1.getIssues()) {
//            assertEquals("Issue " + issueCount, issue.getTitle());
//            assertEquals(issueCount, issue.getId());
//            issueCount++;
//        }
//        // Labels
//        int labelCount = 1;
//        for (TurboLabel label : model1.getLabels()) {
//            assertEquals("Label " + labelCount, label.getActualName());
//            labelCount++;
//        }
//        // Milestones
//        int milestoneCount = 1;
//        for (TurboMilestone milestone : model1.getMilestones()) {
//            assertEquals("Milestone " + milestoneCount, milestone.getTitle());
//            milestoneCount++;
//        }
//        // Users
//        int userCount = 1;
//        for (TurboUser user : model1.getUsers()) {
//            assertEquals("User " + userCount, user.getLoginName());
//            userCount++;
//        }
//    }
//
//    @Test
//    public void operations() {
//        // ID
//        assertEquals(Optional.empty(), model1.getIssueById(11));
//        assertEquals("Issue 10", model1.getIssueById(10).get().getTitle());
//        // Label
//        assertEquals(Optional.empty(), model1.getLabelByName("Label 11"));
//        assertEquals("Label 10", model1.getLabelByName("Label 10").get().getActualName());
//        // User
//        assertEquals(Optional.empty(), model1.getUserByLogin("User 11"));
//        assertEquals("User 10", model1.getUserByLogin("User 10"));
//        // Milestone
//        assertEquals(Optional.empty(), model1.getMilestoneById(11));
//        assertEquals("Milestone 10", model1.getMilestoneById(10).get().getTitle());
//        assertEquals(Optional.empty(), model1.getMilestoneByTitle("Milestone 11"));
//        assertEquals("Milestone 10", model1.getMilestoneByTitle("Milestone 10").get().getTitle());
//        // TODO test get milestone, label(s), assignee(s) of an issue
//    }
//}
