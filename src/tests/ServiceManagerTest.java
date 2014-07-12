package tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Date;

import org.eclipse.egit.github.core.Issue;
import org.junit.BeforeClass;
import org.junit.Test;

import service.ServiceManager;

public class ServiceManagerTest {
	public static String TEST_GH_USERNAME = "testapi";
	public static String TEST_GH_PASSWORD = "hubAPItest1";
	public static String TEST_REPO_NAME = "issuetest";
	
	private static ServiceManager service;
	
	public Issue createTestIssue(){
		Issue test = new Issue();
		test.setTitle("Test Issue");
		return test;
	}
	
	@BeforeClass
	public static void testLogin() {
		service = ServiceManager.getInstance();
		boolean wrongCred = service.login(TEST_GH_USERNAME, "123");
		assertFalse(wrongCred);
		assertTrue(service.login(TEST_GH_USERNAME, TEST_GH_PASSWORD));
		service.setupRepository(TEST_GH_USERNAME, TEST_REPO_NAME);
		service.stopModelUpdate();
	}
	
//	@Test
	public void testCreateIssue(){
		try {
			assertNotNull(service.createIssue(createTestIssue()));
		} catch (IOException e) {
			fail();
		}
	}
	
	@Test
	public void testModifyIssueTitle(){
		Date now = new Date();
		try {
			//TODO: fully automate this
			service.editIssueTitle(1, "test " + now);
		} catch (IOException e) {
			fail();
		}
	}
}
