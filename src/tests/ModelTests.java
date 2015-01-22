package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import javafx.collections.ListChangeListener;
import model.TurboIssue;
import model.TurboLabel;
import model.TurboMilestone;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.RepositoryId;
import org.junit.Test;

import tests.stubs.ModelStub;

@SuppressWarnings("unused")
public class ModelTests {

	private void ______MODEL_FUNCTIONALITY______() {
	}

	@Test
	public void loadingFromCache() {
		ModelStub model = new ModelStub();
		model.loadComponents(new RepositoryId("test", "testing"));

		assertEquals(model.getLabels().size(), 10);
		assertEquals(model.getMilestones().size(), 10);
		assertEquals(model.getCollaborators().size(), 10);
		assertEquals(model.getIssues().size(), 10);
	}

	@Test
	public void loadingFromGitHub() {
		ModelStub model = new ModelStub();
		model.loadComponents(new RepositoryId("test", "testing"));

		assertEquals(model.getLabels().size(), 10);
		assertEquals(model.getMilestones().size(), 10);
		assertEquals(model.getCollaborators().size(), 10);
		assertEquals(model.getIssues().size(), 10);
	}

	int numberOfUpdates = 0;

	private void ______ISSUES______() {
	}

	@Test
	public void loadIssuesTest() {
		ModelStub model = new ModelStub();
		assertEquals(model.getIssues().size(), 0);

		int start = numberOfUpdates;
		ListChangeListener<TurboIssue> listener = c -> ++numberOfUpdates;
		model.getIssuesRef().addListener(listener);
		model.loadIssues(TestUtils.getStubIssues(10));
		model.getIssuesRef().removeListener(listener);
		int end = numberOfUpdates;

		// All issues loaded
		assertEquals(model.getIssues().size(), 10);

		// Only one update triggered
		assertEquals(end - start, 1);
	}

	@Test
	public void getIndexOfIssueTest() {
		ModelStub model = new ModelStub();
		model.loadIssues(TestUtils.getStubIssues(10));

		for (int i = 1; i <= 10; i++) {
			assertEquals(model.getIndexOfIssue(i), i - 1);
		}
	}

	@Test
	public void getIssueWithIdTest() {
		ModelStub model = new ModelStub();
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
		ModelStub model = new ModelStub();
		assertEquals(model.getIssues().size(), 0);

		int start = numberOfUpdates;
		ListChangeListener<TurboIssue> listener = c -> ++numberOfUpdates;
		model.getIssuesRef().addListener(listener);
		model.loadTurboIssues(TestUtils.getStubTurboIssues(model, 10));
		model.getIssuesRef().removeListener(listener);
		int end = numberOfUpdates;

		// All issues loaded
		assertEquals(model.getIssues().size(), 10);

		// Only one update triggered
		assertEquals(end - start, 1);
	}

	@Test
	public void appendToCachedIssuesTest() {
		ModelStub model = new ModelStub();
		model.loadIssues(TestUtils.getStubIssues(10));
		TurboIssue issue11 = TestUtils.getStubTurboIssue(model, 11);
		model.appendToCachedIssues(issue11);
		assertTrue(model.getIssues().size() > 0);
		assertEquals(model.getIssueWithId(11), issue11);
		assertEquals(model.getIssues().get(0), issue11);
	}

	@Test
	public void updateCachedIssuesTest() {
		ModelStub model = new ModelStub();
		model.loadIssues(TestUtils.getStubIssues(10));

		Issue issue1 = TestUtils.getStubIssue(3);
		issue1.setTitle("something different");

		Issue issue2 = TestUtils.getStubIssue(11);
		issue2.setTitle("something really different");

		assertEquals(model.getIssueWithId(3).getTitle(), "issue3");
		assertEquals(model.getIssueWithId(11), null);

		model.updateCachedIssues(Arrays.asList(issue1, issue2), "testing/test");

		// 3 is there and has been changed
		// 11 is not there but is there after
		assertEquals(model.getIssueWithId(3).getTitle(), "something different");
		assertEquals(model.getIssueWithId(11).getTitle(), "something really different");
		assertEquals(model.getIssueWithId(11), model.getIssues().get(0));
	}

	@Test
	public void updateCachedIssueTest() {

		ModelStub model = new ModelStub();
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
		ModelStub model = new ModelStub();
		model.loadLabels(TestUtils.getStubLabels(10));
		for (int i = 0; i < model.getLabels().size(); i++) {
			assertEquals(model.getLabels().get(i), model.getLabelByGhName("group.label" + (i + 1)));
		}
	}

	@Test
	public void addLabelTest() {
		ModelStub model = new ModelStub();
		model.loadLabels(TestUtils.getStubLabels(10));
		TurboLabel newLabel = TestUtils.getStubTurboLabel("group", "test");
		model.addLabel(newLabel);
		assertNotEquals(model.getLabels().size(), 0);
		assertEquals(model.getLabels().get(model.getLabels().size() - 1), newLabel);
	}

	@Test
	public void isExclusiveLabelTest() {
		ModelStub model = new ModelStub();
		List<TurboLabel> labels = TestUtils.getStubTurboLabels(10);
		labels.forEach(l -> l.setExclusive(true));
		
		model.loadTurboLabels(labels);
		assertTrue(model.isExclusiveLabelGroup("group"));
		
		labels.get(0).setExclusive(false);
		assertFalse(model.isExclusiveLabelGroup("group"));
	}

	@Test
	public void deleteLabelTest() {
		ModelStub model = new ModelStub();
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
		ModelStub model = new ModelStub();
		assertEquals(model.getLabels().size(), 0);

		int start = numberOfUpdates;
		ListChangeListener<TurboLabel> listener = c -> ++numberOfUpdates;
		model.getLabelsRef().addListener(listener);
		model.loadLabels(TestUtils.getStubLabels(10));
		model.getLabelsRef().removeListener(listener);
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
		ModelStub model = new ModelStub();
		assertEquals(model.getLabels().size(), 0);

		int start = numberOfUpdates;
		ListChangeListener<TurboLabel> listener = c -> ++numberOfUpdates;
		model.getLabelsRef().addListener(listener);
		model.loadTurboLabels(TestUtils.getStubTurboLabels(10));
		model.getLabelsRef().removeListener(listener);
		int end = numberOfUpdates;

		// All issues loaded
		assertEquals(model.getLabels().size(), 10);

		// Only one update triggered
		assertEquals(end - start, 1);
	}

	@Test
	public void updateCachedLabelsTest() {
		ModelStub model = new ModelStub();
		List<Label> labels = TestUtils.getStubLabels(10);
		model.loadLabels(labels);

		Label label1 = labels.get(3);
		label1.setName("something");

		Label label2 = TestUtils.getStubLabel("something else");

		assertEquals(model.getLabels().size(), 10);
		assertEquals(model.getLabels().get(3).getName(), "label4");
		assertEquals(model.getLabelByGhName("something else"), null);

		model.updateCachedLabels(Arrays.asList(label1, label2), "testing/test");
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
		ModelStub model = new ModelStub();
		model.loadIssues(TestUtils.getStubIssues(10));
		TurboMilestone newMilestone = TestUtils.getStubTurboMilestone("milestone123");
		model.addMilestone(newMilestone);
		assertEquals(model.getMilestones().get(model.getMilestones().size() - 1), newMilestone);
	}

	@Test
	public void deleteMilestoneTest() {
		// TODO
	}

	@Test
	public void loadMilestonesTest() {
		// TODO
	}

	@Test
	public void getMilestoneByTitleTest() {
		// TODO
	}

	private void ______CACHED_MILESTONES______() {
	}

	@Test
	public void loadTurboMilestonesTest() {
		// TODO
	}

	@Test
	public void updateCachedMilestonesTest() {
		// TODO
	}

	private void ______COLLABORATORS______() {
	}

	@Test
	public void getUserByGhName() {
		// TODO
	}

	@Test
	public void loadCollaborators() {
		// TODO
	}

	@Test
	public void clearCollaborators() {
		// TODO
	}

	private void ______CACHED_COLLABORATORS______() {
	}

	@Test
	public void loadTurboCollaborators() {
		// TODO
	}

	@Test
	public void updateCachedCollaborators() {
		// TODO
	}

	private void ______RESOURCE_METADATA______() {
	}

	// TODO enforce invariants rather than test getters

}
