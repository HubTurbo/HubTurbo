package tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Date;

import org.eclipse.egit.github.core.Issue;
import org.junit.BeforeClass;
import org.junit.Test;

import service.ServiceManager;
import tests.stubs.ServiceManagerStub;

public class ServiceManagerTests {

	public static String TEST_GITHUB_USERNAME = "testapi";
	private static String TEST_GITHUB_PASSWORD = "hubAPItest1";
	
	public static String TEST_REPO_NAME = "issuetest";

	private static ServiceManager serviceManager;
	
	@BeforeClass
	public static void setup() {
		ServiceManager.isInTestMode = true;
		serviceManager = (ServiceManagerStub) ServiceManager.getInstance();
		testLogin();
	}
	
	public static void testLogin() {
		boolean wrongCred = serviceManager.login(TEST_GITHUB_USERNAME, "123");
		assertFalse(wrongCred);
		assertTrue(serviceManager.login(TEST_GITHUB_USERNAME, TEST_GITHUB_PASSWORD));
		try {
			serviceManager.setupRepository(TEST_GITHUB_USERNAME, TEST_REPO_NAME);
		} catch (IOException e) {
			fail();
		}
	}
	
	@Test
	public void setupRepositoryTest() {
		try {
			serviceManager.setupRepository(TEST_GITHUB_USERNAME, TEST_REPO_NAME);
		} catch (IOException e) {
			fail();
		}
		
		// Size is due to loading stub data
		assertEquals(10, serviceManager.getModel().getIssues().size());
	}

	@Test
	public void testCreateIssue() {
		try {
			assertNotNull(serviceManager.createIssue(TestUtils.getStubIssue(1)));
		} catch (IOException e) {
			fail();
		}
	}

	@Test
	public void testModifyIssueTitle() {
	    Date now = new Date();
	    try {
	        // TODO: fully automate this
	        serviceManager.editIssueTitle(1, "test " + now);
	    } catch (IOException e) {
	        fail();
	    }
	}

	@Test
	public void testModifyIssueDescription() {
	    Date now = new Date();
	    try {
	        // TODO: fully automate this
	        String desc = "desc " + now;
	        serviceManager.editIssueBody(1, desc);

	    } catch (IOException e) {
	        fail(e.getMessage());
	    }
	}

	@Test
	public void testChangeIssueState() {
	    try {
	        Issue issue = serviceManager.getIssue(1);
	        assertTrue(issue != null);
	        if (issue.getState().equals(ServiceManager.STATE_OPEN)) {
	            closeAndOpenIssue(1);
	        } else {
	            openAndCloseIssue(1);
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	        fail();
	    }
	}

	private void openAndCloseIssue(int issueId) {
	    try {
	        testOpenIssue(1);
	        testCloseIssue(1);
	    } catch (IOException e) {
	        fail(e.getMessage());
	    }
	}

	private void testOpenIssue(int issueId) throws IOException {
	    serviceManager.openIssue(1);
	    String newState = serviceManager.getIssue(1).getState();
	    assertTrue(newState.equals(ServiceManager.STATE_OPEN));
	}

	private void testCloseIssue(int issueId) throws IOException {
	    serviceManager.closeIssue(1);
	    String newState = serviceManager.getIssue(1).getState();
	    assertTrue(newState.equals(ServiceManager.STATE_CLOSED));
	}

	private void closeAndOpenIssue(int issueId) {
	    try {
	        testCloseIssue(1);
	        testOpenIssue(1);
	    } catch (IOException e) {
	        fail(e.getMessage());
	    }
	}
}
