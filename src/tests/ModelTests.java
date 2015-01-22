package tests;

import static org.junit.Assert.assertEquals;

import org.eclipse.egit.github.core.RepositoryId;
import org.junit.Test;

import tests.stubs.ModelStub;

public class ModelTests {

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
	
}
