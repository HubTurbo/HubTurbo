package tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Date;

import org.eclipse.egit.github.core.Issue;
import org.junit.BeforeClass;
import org.junit.Test;

import service.ServiceManager;

public class ServiceManagerTests {
	public static String TEST_GH_USERNAME = "testapi";
	public static String TEST_GH_PASSWORD = "hubAPItest1";
	public static String TEST_REPO_NAME = "issuetest";
	public static final String STATE_OPEN = "open"; //$NON-NLS-1$
	public static final String STATE_CLOSED = "closed"; //$NON-NLS-1$

	
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
		try {
			service.setupRepository(TEST_GH_USERNAME, TEST_REPO_NAME);
		} catch (IOException e) {
			fail();
		}
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
	
	@Test
	public void testModifyIssueDescription(){
		Date now = new Date();
		try {
			//TODO: fully automate this
			String desc = "desc " + now;
			service.editIssueBody(1, desc);
			
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testChangeIssueState(){
		try {
			Issue issue = service.getIssue(1);
			assertTrue(issue != null);
			if(issue.getState().equals(STATE_OPEN)){
				closeAndOpenIssue(1);
			}else{
				openAndCloseIssue(1);
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	private void openAndCloseIssue(int issueId){
		try {	
			testOpenIssue(1);
			testCloseIssue(1);
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
	
	private void testOpenIssue(int issueId) throws IOException{
		service.openIssue(1);
		String newState = service.getIssue(1).getState();
		assertTrue(newState.equals(STATE_OPEN));
	}
	
	private void testCloseIssue(int issueId) throws IOException{
		service.closeIssue(1);
		String newState = service.getIssue(1).getState();
		assertTrue(newState.equals(STATE_CLOSED));
	}
	
	private void closeAndOpenIssue(int issueId){
		try {
			testCloseIssue(1);
			testOpenIssue(1);
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
}
