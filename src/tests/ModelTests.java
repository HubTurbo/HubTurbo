package tests;

import static org.junit.Assert.assertEquals;
import javafx.collections.ListChangeListener;
import model.TurboIssue;

import org.eclipse.egit.github.core.RepositoryId;
import org.junit.BeforeClass;
import org.junit.Test;

import service.ServiceManager;
import storage.ConfigFileHandler;
import storage.DataManager;
import tests.stubs.ModelStub;

@SuppressWarnings("unused")
public class ModelTests {

//	@BeforeClass
//	public static void setup() {
//		ServiceManager.isTestMode = true;
//	}

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
	
	private void ______ISSUES______() {
	}
	
	int numberOfUpdates = 0;
	
	@Test
	public void loadIssuesTest() {
		ModelStub model = new ModelStub();
		assert model.getIssues().size() == 0;
		
		int start = numberOfUpdates;
		ListChangeListener<TurboIssue> listener = c -> ++numberOfUpdates;
		model.getIssuesRef().addListener(listener);
		model.loadIssues(TestUtils.getStubIssues(10));
		model.getIssuesRef().removeListener(listener);
		int end = numberOfUpdates;
		
		// All issues loaded
		assert model.getIssues().size() == 10;
		
		// Only one update triggered
		assert end - start == 1;
	}

	@Test
	public void getIndexOfIssueTest() {

	}

	@Test
	public void getIssueWithIdTest() {
	}
	
	private void ______CACHED_ISSUES______() {
	}
	private void ______LABELS______() {
	}
	private void ______CACHED_LABELS______() {
	}
	private void ______MILESTONES______() {
	}
	private void ______CACHED_MILESTONES______() {
	}
	private void ______COLLABORATORS______() {
	}
	private void ______CACHED_COLLABORATORS______() {
	}
	private void ______RESOURCE_METADATA______() {
	}
		
}
