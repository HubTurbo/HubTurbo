package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import model.Model;
import model.TurboIssue;
import model.TurboLabel;
import model.TurboMilestone;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;
import org.junit.BeforeClass;
import org.junit.Test;

import tests.stubs.ModelEventDispatcherStub;
import ui.UI;
import util.events.EventHandler;
import util.events.ModelChangedEvent;
import util.events.ModelChangedEventHandler;

import com.google.common.eventbus.EventBus;

@SuppressWarnings("unused")
public class ModelTests {

	private static final String TEST_REPO_OWNER = "test";
	private static final String TEST_REPO_NAME = "testing";

	@BeforeClass
	public static void setup() {
		Model.isInTestMode = true;
	}

	@Test(expected = UnsupportedOperationException.class)
	public void mutationOfIssues() {
		Model model = new Model();
		model.getIssues().add(TestUtils.getStubTurboIssue(model, 1));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void mutationOfMilestones() {
		Model model = new Model();
		model.getMilestones().add(TestUtils.getStubTurboMilestone("this"));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void mutationOfLabels() {
		Model model = new Model();
		model.getLabels().add(TestUtils.getStubTurboLabel("does", "not"));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void mutationOfUsers() {
		Model model = new Model();
		model.getCollaborators().add(TestUtils.getStubTurboUser("matter"));
	}

	private void ______MODEL_FUNCTIONALITY______() {
	}

	@Test
	public void loadingFromCache() throws IOException {
		Model model = new Model();
		model.loadComponents(new RepositoryId(TEST_REPO_OWNER, TEST_REPO_NAME));

		assertEquals(model.getLabels().size(), 10);
		assertEquals(model.getMilestones().size(), 10);
		assertEquals(model.getCollaborators().size(), 10);
		assertEquals(model.getIssues().size(), 10);
	}

	@Test
	public void loadingFromGitHub() throws IOException {
		Model model = new Model();
		model.loadComponents(new RepositoryId(TEST_REPO_OWNER, TEST_REPO_NAME));

		assertEquals(model.getLabels().size(), 10);
		assertEquals(model.getMilestones().size(), 10);
		assertEquals(model.getCollaborators().size(), 10);
		assertEquals(model.getIssues().size(), 10);
	}

	private int numberOfUpdates = 0;
	private EventHandler modelChangedHandler = null;

	private void registerChangeEvent(Model model) {
		assert modelChangedHandler == null;
		model.getTestEvents().register(modelChangedHandler = new ModelChangedEventHandler() {
			@Override
			public void handle(ModelChangedEvent e) {
				++numberOfUpdates;
			}
		});
	}

	private void unregisterChangeEvent(Model model) {
		assert modelChangedHandler != null;
		model.getTestEvents().unregister(modelChangedHandler);
	}

	private void ______ISSUES______() {
	}

	@Test
	public void loadIssuesTest() {
		Model model = new Model();
		assertEquals(model.getIssues().size(), 0);

		int start = numberOfUpdates;
		registerChangeEvent(model);
		model.loadIssues(TestUtils.getStubIssues(10));
		unregisterChangeEvent(model);
		int end = numberOfUpdates;

		// All issues loaded
		assertEquals(model.getIssues().size(), 10);

		// Only one update triggered
		assertEquals(1, end - start);
	}

	@Test
	public void getIndexOfIssueTest() {
		Model model = new Model();
		model.loadIssues(TestUtils.getStubIssues(10));

		for (int i = 1; i <= 10; i++) {
			assertEquals(model.getIndexOfIssue(i), i - 1);
		}
	}

	@Test
	public void getIssueWithIdTest() {
		Model model = new Model();
		model.loadIssues(TestUtils.getStubIssues(10));

		for (int i = 1; i <= 10; i++) {
			assertNotEquals(model.getIssueWithId(i), null);
			assertTrue(model.getIssueWithId(i).getTitle().endsWith(i + ""));
		}
	}

	private void ______CACHED_ISSUES______() {
	}

	@Test
	public void loadTurboIssuesTest() {
		Model model = new Model();
		assertEquals(model.getIssues().size(), 0);

		int start = numberOfUpdates;
		registerChangeEvent(model);
		model.loadTurboIssues(TestUtils.getStubTurboIssues(model, 10));
		unregisterChangeEvent(model);
		int end = numberOfUpdates;

		// All issues loaded
		assertEquals(model.getIssues().size(), 10);

		// Only one update triggered
		assertEquals(1, end - start);
	}

	@Test
	public void appendToCachedIssuesTest() {
		Model model = new Model();
		model.loadIssues(TestUtils.getStubIssues(10));
		TurboIssue issue11 = TestUtils.getStubTurboIssue(model, 11);
		model.appendToCachedIssues(issue11);
		assertTrue(model.getIssues().size() > 0);
		assertEquals(model.getIssueWithId(11), issue11);
		assertEquals(model.getIssues().get(0), issue11);
	}

	@Test
	public void updateCachedIssuesTest() {
		Model model = new Model();
		model.loadIssues(TestUtils.getStubIssues(10));

		int start = numberOfUpdates;
		registerChangeEvent(model);

		Issue issue1 = TestUtils.getStubIssue(3);
		issue1.setTitle("something different");

		Issue issue2 = TestUtils.getStubIssue(11);
		issue2.setTitle("something really different");

		assertEquals(model.getIssueWithId(3).getTitle(), "issue3");
		assertEquals(model.getIssueWithId(11), null);

		model.updateCachedIssues(new CountDownLatch(4), Arrays.asList(issue1, issue2), "testing/test");

		// 3 is there and has been changed
		// 11 is not there but is there after
		assertEquals(model.getIssueWithId(3).getTitle(), "something different");
		assertEquals(model.getIssueWithId(11).getTitle(), "something really different");
		assertEquals(model.getIssueWithId(11), model.getIssues().get(0));

		unregisterChangeEvent(model);
		int end = numberOfUpdates;

		// Only one update triggered
		assertEquals(1, end - start);
	}

	@Test
	public void updateCachedIssueTest() {

		Model model = new Model();
		model.loadIssues(TestUtils.getStubIssues(10));

		TurboIssue issue = TestUtils.getStubTurboIssue(model, 3);
		issue.setTitle("something different");
		// 3 is there
		assertEquals(model.getIssueWithId(3).getTitle(), "issue3");
		model.updateCachedIssue(issue);
		// 3 has been changed
		assertEquals(model.getIssueWithId(3).getTitle(), "something different");

		issue = TestUtils.getStubTurboIssue(model, 11);
		issue.setTitle("something really different");
		// 11 is not there
		assertEquals(model.getIssueWithId(11), null);
		model.updateCachedIssue(issue);
		// 11 has been added, and to the front
		assertEquals(model.getIssueWithId(11).getTitle(), "something really different");
		assertEquals(model.getIssueWithId(11), model.getIssues().get(0));
	}

	private void ______LABELS______() {
	}

	@Test
	public void getLabelByGhNameTest() {
		Model model = new Model();
		model.loadLabels(TestUtils.getStubLabels(10));
		for (int i = 0; i < model.getLabels().size(); i++) {
			assertEquals(model.getLabels().get(i), model.getLabelByGhName("group.label" + (i + 1)));
		}
	}

	@Test
	public void addLabelTest() {
		Model model = new Model();
		model.loadLabels(TestUtils.getStubLabels(10));
		TurboLabel newLabel = TestUtils.getStubTurboLabel("group", "name");
		model.addLabel(newLabel);
		assertNotEquals(model.getLabels().size(), 0);
		assertEquals(model.getLabels().get(model.getLabels().size() - 1), newLabel);
	}

	@Test
	public void isExclusiveLabelTest() {
		Model model = new Model();
		List<TurboLabel> labels = TestUtils.getStubTurboLabels(10);
		labels.forEach(l -> l.setExclusive(true));

		model.loadTurboLabels(labels);
		assertTrue(model.isExclusiveLabelGroup("group"));

		labels.get(0).setExclusive(false);
		assertFalse(model.isExclusiveLabelGroup("group"));
	}

	@Test
	public void deleteLabelTest() {
		Model model = new Model();
		model.loadTurboLabels(TestUtils.getStubTurboLabels(10));
		model.deleteLabel(model.getLabels().get(2)); // ids 3 and 5
		model.deleteLabel(model.getLabels().get(4));

		assertEquals(model.getLabels().size(), 8);
		for (TurboLabel label : model.getLabels()) {
			if (label.getName().endsWith("3") || label.getName().endsWith("5")) {
				assert false;
			}
		}
	}

	@Test
	public void loadLabelsTest() {
		Model model = new Model();
		assertEquals(model.getLabels().size(), 0);

		int start = numberOfUpdates;
		registerChangeEvent(model);
		model.loadLabels(TestUtils.getStubLabels(10));
		unregisterChangeEvent(model);
		int end = numberOfUpdates;

		// All issues loaded
		assertEquals(model.getLabels().size(), 10);

		// Only one update triggered
		assertEquals(end - start, 1);
	}

	private void ______CACHED_LABELS______() {
	}

	@Test
	public void loadTurboLabelsTest() {
		Model model = new Model();
		assertEquals(model.getLabels().size(), 0);

		int start = numberOfUpdates;
		registerChangeEvent(model);
		model.loadTurboLabels(TestUtils.getStubTurboLabels(10));
		unregisterChangeEvent(model);
		int end = numberOfUpdates;

		// All issues loaded
		assertEquals(model.getLabels().size(), 10);

		// Only one update triggered
		assertEquals(end - start, 1);
	}

	@Test
	public void updateCachedLabelsTest() {
		Model model = new Model();
		List<Label> labels = TestUtils.getStubLabels(10);
		model.loadLabels(labels);

		Label label1 = labels.get(3);
		label1.setName("something");

		Label label2 = TestUtils.getStubLabel("something else");

		assertEquals(model.getLabels().size(), 10);
		assertEquals(model.getLabels().get(3).getName(), "label4");
		assertEquals(model.getLabelByGhName("something else"), null);

		model.updateCachedLabels(new CountDownLatch(4), Arrays.asList(label1, label2), "testing/test");
		assertEquals(model.getLabels().size(), 2);

		// label1 is there and has been changed
		// label2 is not there but is there after
		// Order is preserved
		// The other labels are gone
		assertEquals(model.getLabels().get(0).getName(), "something");
		assertEquals(model.getLabels().get(1).getName(), "something else");
	}

	private void ______MILESTONES______() {
	}

	@Test
	public void addMilestoneTest() {
		Model model = new Model();
		model.loadIssues(TestUtils.getStubIssues(10));
		TurboMilestone newMilestone = TestUtils.getStubTurboMilestone("milestone123");
		model.addMilestone(newMilestone);
		assertEquals(model.getMilestones().get(model.getMilestones().size() - 1), newMilestone);
	}

	@Test
	public void deleteMilestoneTest() {
		Model model = new Model();
		model.loadTurboMilestones(TestUtils.getStubTurboMilestones(10));

		model.deleteMilestone(model.getMilestones().get(2)); // ids 3 and 5
		model.deleteMilestone(model.getMilestones().get(4));

		assertEquals(model.getMilestones().size(), 8);
		for (TurboMilestone milestone : model.getMilestones()) {
			if (milestone.getTitle().endsWith("3") || milestone.getTitle().endsWith("5")) {
				assert false;
			}
		}
	}

	@Test
	public void loadMilestonesTest() {
		Model model = new Model();
		assertEquals(model.getMilestones().size(), 0);

		int start = numberOfUpdates;
		registerChangeEvent(model);
		model.loadMilestones(TestUtils.getStubMilestones(10));
		unregisterChangeEvent(model);
		int end = numberOfUpdates;

		// All issues loaded
		assertEquals(model.getMilestones().size(), 10);

		// Only one update triggered
		assertEquals(end - start, 1);
	}

	@Test
	public void getMilestoneByTitleTest() {
		Model model = new Model();
		model.loadMilestones(TestUtils.getStubMilestones(10));
		for (int i = 0; i < model.getMilestones().size(); i++) {
			assertEquals(model.getMilestones().get(i), model.getMilestoneByTitle("v0." + (i + 1)));
		}
	}

	private void ______CACHED_MILESTONES______() {
	}

	@Test
	public void loadTurboMilestonesTest() {
		Model model = new Model();
		assertEquals(model.getMilestones().size(), 0);

		int start = numberOfUpdates;
		registerChangeEvent(model);
		model.loadTurboMilestones(TestUtils.getStubTurboMilestones(10));
		unregisterChangeEvent(model);
		int end = numberOfUpdates;

		// All issues loaded
		assertEquals(model.getMilestones().size(), 10);

		// Only one update triggered
		assertEquals(end - start, 1);
	}

	@Test
	public void updateCachedMilestonesTest() {
	    Model model = new Model();
	    List<Milestone> milestones = TestUtils.getStubMilestones(10);
	    model.loadMilestones(milestones);

	    Milestone milestone1 = milestones.get(3);
	    milestone1.setTitle("amilestone");

	    Milestone milestone2 = TestUtils.getStubMilestone("anothermilestone");

	    assertEquals(model.getMilestones().size(), 10);
	    assertEquals(model.getMilestones().get(3).getTitle(), "v0.4");
	    assertEquals(model.getMilestoneByTitle("anothermilestone"), null);

	    model.updateCachedMilestones(new CountDownLatch(4), Arrays.asList(milestone1, milestone2), "testing/test");
	    assertEquals(model.getMilestones().size(), 2);

	    // milestone1 is there and has been changed
	    // milestone2 is not there but is there after
	    // Order is preserved
	    // The other milestones are gone
	    assertEquals(model.getMilestones().get(0).getTitle(), "amilestone");
	    assertEquals(model.getMilestones().get(1).getTitle(), "anothermilestone");
	}

	private void ______COLLABORATORS______() {
	}

	@Test
	public void getUserByGhNameTest() {
		Model model = new Model();
		model.loadCollaborators(TestUtils.getStubUsers(10));
		for (int i = 0; i < model.getCollaborators().size(); i++) {
			assertEquals(model.getCollaborators().get(i), model.getUserByGhName("user" + (i+1)));
		}
	}

	@Test
	public void loadCollaboratorsTest() {
		Model model = new Model();
		assertEquals(model.getCollaborators().size(), 0);

		int start = numberOfUpdates;
		registerChangeEvent(model);
		model.loadCollaborators(TestUtils.getStubUsers(10));
		unregisterChangeEvent(model);
		int end = numberOfUpdates;

		// All issues loaded
		assertEquals(model.getCollaborators().size(), 10);

		// Only one update triggered
		assertEquals(1, end - start);
	}

	@Test
	public void clearCollaboratorsTest() {
		Model model = new Model();
		assertEquals(model.getCollaborators().size(), 0);
		model.loadTurboCollaborators(TestUtils.getStubTurboUsers(10));
		assertEquals(model.getCollaborators().size(), 10);
		model.clearCollaborators();
		assertEquals(model.getCollaborators().size(), 0);
	}

	private void ______CACHED_COLLABORATORS______() {
	}

	@Test
	public void loadTurboCollaboratorsTest() {
		Model model = new Model();
		assertEquals(model.getCollaborators().size(), 0);

		int start = numberOfUpdates;
		registerChangeEvent(model);
		model.loadTurboCollaborators(TestUtils.getStubTurboUsers(10));
		unregisterChangeEvent(model);
		int end = numberOfUpdates;

		// All issues loaded
		assertEquals(model.getCollaborators().size(), 10);

		// Only one update triggered
		assertEquals(end - start, 1);
	}

	@Test
	public void updateCachedCollaboratorsTest() {
	    Model model = new Model();

	    List<User> milestones = TestUtils.getStubUsers(10);
	    model.loadCollaborators(milestones);

	    User user1 = milestones.get(3);
	    user1.setLogin("auser");

        User user2 = TestUtils.getStubUser("anotheruser");

        assertEquals(model.getCollaborators().size(), 10);
        assertEquals(model.getCollaborators().get(3).getGithubName(), "user4");
        assertEquals(model.getUserByGhName("anotheruser"), null);

        model.updateCachedCollaborators(new CountDownLatch(4), Arrays.asList(user1, user2), "testing/test");
        assertEquals(model.getCollaborators().size(), 2);

        // user1 is there and has been changed
        // user2 is not there but is there after
        // Order is preserved
        // The other milestones are gone
        assertEquals(model.getCollaborators().get(0).getGithubName(), "auser");
        assertEquals(model.getCollaborators().get(1).getGithubName(), "anotheruser");
	}

	private void ______RESOURCE_METADATA______() {
	}

	// TODO enforce invariants rather than test getters

}
