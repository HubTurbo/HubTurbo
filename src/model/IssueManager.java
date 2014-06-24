package model;

import java.util.ArrayList;
import java.util.List;

public class IssueManager {
	private List<TurboIssue> issues;
	
	IssueManager() {
		this.issues = new ArrayList<TurboIssue>(); 
	}

	public List<TurboIssue> getIssues() {
		return issues;
	}
	
/*	
	TurboIssue getIssue(IRepositoryIdProvider repository, int issueNumber) {
		Issue issue = null;
		try {
			issue = service.getIssue(repository, issueNumber);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new TurboIssue(issue);
	}
	
	public void createIssue(Issue issue) {
		
	}
*/
}
