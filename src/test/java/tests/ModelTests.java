package tests;

import backend.UpdateSignature;
import backend.resource.*;
import backend.resource.serialization.SerializableModel;
import backend.stub.DummyRepo;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.*;

public class ModelTests {

	private static final String REPO = "test/test";

    private static Model modelEmptySig, modelEmptySig2, modelUpdated, modelCopyUpdated, modelCopyNotUpdated;

    @BeforeClass
    public static void initialize() {
        DummyRepo dummy = new DummyRepo();

        modelEmptySig = new Model(REPO,
                dummy.getIssues(REPO),
                dummy.getLabels(REPO),
                dummy.getMilestones(REPO),
                dummy.getCollaborators(REPO));

	    // Explicit declaration of empty signature
	    modelEmptySig2 = new Model(REPO,
		    dummy.getIssues(REPO),
		    dummy.getLabels(REPO),
		    dummy.getMilestones(REPO),
		    dummy.getCollaborators(REPO));

        modelUpdated = new Model(REPO,
                dummy.getIssues(REPO),
                dummy.getLabels(REPO),
                dummy.getMilestones(REPO),
                dummy.getCollaborators(REPO),
                new UpdateSignature("issues", "labels", "milestones", "collaborators", new Date()));

        modelCopyUpdated = new Model(modelUpdated);
	    modelCopyNotUpdated = new Model(modelEmptySig);
    }

	@Test
	public void immutability() {
		Model other = new Model(modelUpdated);
		other.getIssues().add(new TurboIssue(REPO, 11, ""));
		assertEquals(modelUpdated, other);

		other = new Model(modelUpdated);
		other.getLabels().add(new TurboLabel(REPO, "aksdjl"));
		assertEquals(modelUpdated, other);

		other = new Model(modelUpdated);
		other.getMilestones().add(new TurboMilestone(REPO, 11, ""));
		assertEquals(modelUpdated, other);

		other = new Model(modelUpdated);
		other.getUsers().add(new TurboUser(REPO, ""));
		assertEquals(modelUpdated, other);
	}

    @Test
    public void equality() {

	    assertEquals(modelUpdated, modelUpdated);
	    assertNotEquals(modelUpdated, null);
	    assertNotEquals(modelUpdated, 1);

        // Empty signature

        assertEquals(modelEmptySig, modelEmptySig2);

        // Copy correctness

        assertEquals(modelCopyUpdated, modelUpdated);
	    assertEquals(modelCopyNotUpdated, modelEmptySig);

	    // Update signature

	    // Tested by changing one element in the model and ensuring inequality

	    assertEquals(modelEmptySig.hashCode(), modelEmptySig2.hashCode());
	    assertEquals(modelCopyUpdated.hashCode(), modelUpdated.hashCode());
	    assertNotEquals(modelEmptySig.hashCode(), modelUpdated.hashCode());

	    Model model = new Model("something", modelUpdated.getIssues(), modelUpdated.getLabels(),
		    modelUpdated.getMilestones(), modelUpdated.getUsers(), modelUpdated.getUpdateSignature());
	    assertNotEquals(model.hashCode(), modelUpdated.hashCode());
	    assertNotEquals(model, modelUpdated);

	    List<TurboIssue> issues = new ArrayList<>(modelUpdated.getIssues());
	    issues.add(new TurboIssue(REPO, 11, "something"));
	    model = new Model(REPO, issues, modelUpdated.getLabels(),
		    modelUpdated.getMilestones(), modelUpdated.getUsers(), modelUpdated.getUpdateSignature());
	    assertNotEquals(model.hashCode(), modelUpdated.hashCode());
	    assertNotEquals(model, modelUpdated);

	    List<TurboLabel> labels = new ArrayList<>(modelUpdated.getLabels());
	    labels.add(new TurboLabel(REPO, "Label 11"));
	    model = new Model(REPO, modelUpdated.getIssues(), labels,
		    modelUpdated.getMilestones(), modelUpdated.getUsers(), modelUpdated.getUpdateSignature());
	    assertNotEquals(model.hashCode(), modelUpdated.hashCode());
	    assertNotEquals(model, modelUpdated);

	    List<TurboMilestone> milestones = new ArrayList<>(modelUpdated.getMilestones());
	    milestones.add(new TurboMilestone(REPO, 11, "something"));
	    model = new Model(REPO, modelUpdated.getIssues(), modelUpdated.getLabels(),
		    milestones, modelUpdated.getUsers(), modelUpdated.getUpdateSignature());
	    assertNotEquals(model.hashCode(), modelUpdated.hashCode());
	    assertNotEquals(model, modelUpdated);

	    List<TurboUser> users = new ArrayList<>(modelUpdated.getUsers());
	    users.add(new TurboUser(REPO, "someone"));
	    model = new Model(REPO, modelUpdated.getIssues(), modelUpdated.getLabels(),
		    modelUpdated.getMilestones(), users, modelUpdated.getUpdateSignature());
	    assertNotEquals(model.hashCode(), modelUpdated.hashCode());
	    assertNotEquals(model, modelUpdated);
    }

	@Test
	public void serialisation() {
		// Test for conversion to and from SerializableModel
		SerializableModel serializedModel = new SerializableModel(modelUpdated);
		Model deserializedModel = new Model(serializedModel);

		// Test for serialization correctness
		assertEquals(modelUpdated, deserializedModel);
	}

    @Test
    public void getters() {
        // ID
        assertEquals(REPO, modelUpdated.getRepoId());
        assertEquals(modelUpdated.getRepoId(), modelUpdated.getRepoId());

        // Signature
        assertEquals(modelEmptySig.getUpdateSignature(), UpdateSignature.empty);
        assertEquals(modelEmptySig.getUpdateSignature(), modelEmptySig2.getUpdateSignature());

        // Resources
		// Issues
        ArrayList<Integer> issueIds = new ArrayList<>();
		for (int i = 1; i <= 10; i++) {
			issueIds.add(i);
		}
		Collections.sort(issueIds); // 1, 2..10
		int issueCount = 1;
        for (TurboIssue issue : modelUpdated.getIssues()) {
	        assertEquals(issueCount, modelUpdated.getIssueById(issueCount).get().getId());
            assertEquals(issueIds.get(issueCount - 1).intValue(), issue.getId());
			issueCount++;
        }

		// Labels
		ArrayList<String> labelNames = new ArrayList<>();
		for (int i = 1; i <= 10; i++) {
			labelNames.add("Label " + i);
		}
		Collections.sort(labelNames); // Label 1, Label 10, Label 2..9
        int labelCount = 1;
        for (TurboLabel label : modelUpdated.getLabels()) {
            assertEquals(labelNames.get(labelCount - 1), label.getActualName());
	        assertEquals("Label " + labelCount,
					modelUpdated.getLabelByActualName("Label " + labelCount).get().getActualName());
            labelCount++;
        }

		// Milestones
		ArrayList<Integer> milestoneIds = new ArrayList<>();
		for (int i = 1; i <= 10; i++) {
			milestoneIds.add(i);
		}
		Collections.sort(milestoneIds); // 1, 2..10
        int milestoneCount = 1;
        for (TurboMilestone milestone : modelUpdated.getMilestones()) {
            assertEquals(milestoneCount, milestone.getId());
	        assertEquals(milestoneIds.get(milestoneCount - 1).intValue(),
					modelUpdated.getMilestoneById(milestoneCount).get().getId());
	        assertEquals("Milestone " + milestoneCount,
		        modelUpdated.getMilestoneByTitle("Milestone " + milestoneCount).get().getTitle());
            milestoneCount++;
        }

		// Users
		ArrayList<String> userLogins = new ArrayList<>();
		for (int i = 1; i <= 10; i++) {
			userLogins.add("User " + i);
		}
		Collections.sort(userLogins); // User 1, User 10, User 2..9
        int userCount = 1;
        for (TurboUser user : modelUpdated.getUsers()) {
            assertEquals(userLogins.get(userCount - 1), user.getLoginName());
	        assertEquals("User " + userCount,
		        modelUpdated.getUserByLogin("User " + userCount).get().getLoginName());
            userCount++;
        }
    }

    @Test
    public void operations() {

	    // Issues

	    try {
		    modelUpdated.getIssueById(0);
	    } catch (AssertionError ignored) {}
	    try {
		    modelUpdated.getIssueById(-1);
	    } catch (AssertionError ignored) {}

        assertEquals(Optional.<TurboIssue>empty(), modelUpdated.getIssueById(11));
        assertEquals("Issue 10", modelUpdated.getIssueById(10).get().getTitle());

	    // Labels

	    try {
		    modelUpdated.getLabelByActualName(null);
	    } catch (AssertionError ignored) {}
	    try {
		    modelUpdated.getLabelByActualName("");
	    } catch (AssertionError ignored) {}

        assertEquals(Optional.<TurboLabel>empty(), modelUpdated.getLabelByActualName("Label 11"));
        assertEquals("Label 10", modelUpdated.getLabelByActualName("Label 10").get().getActualName());

	    // Milestones

	    try {
		    modelUpdated.getMilestoneById(-1);
	    } catch (AssertionError ignored) {}
	    try {
		    modelUpdated.getMilestoneById(0);
	    } catch (AssertionError ignored) {}


	    try {
		    modelUpdated.getMilestoneByTitle(null);
	    } catch (AssertionError ignored) {}
	    try {
		    modelUpdated.getMilestoneByTitle("");
	    } catch (AssertionError ignored) {}

	    assertEquals(Optional.<TurboMilestone>empty(), modelUpdated.getMilestoneById(11));
	    assertEquals("Milestone 10", modelUpdated.getMilestoneById(10).get().getTitle());

	    assertEquals(Optional.<TurboMilestone>empty(), modelUpdated.getMilestoneByTitle("Milestone 11"));
	    assertEquals("Milestone 10", modelUpdated.getMilestoneByTitle("Milestone 10").get().getTitle());

	    // Users

	    try {
		    modelUpdated.getUserByLogin(null);
	    } catch (AssertionError ignored) {}
	    try {
		    modelUpdated.getUserByLogin("");
	    } catch (AssertionError ignored) {}

        assertEquals(Optional.<TurboUser>empty(), modelUpdated.getUserByLogin("User 11"));
        assertEquals("User 10", modelUpdated.getUserByLogin("User 10").get().getLoginName());
    }
}
