package model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;

public class IssueManager {
	private IssueService service;
	private GitHubClient client;
	
	IssueManager(GitHubClient client) {
		this.client = client;
		service = new IssueService(this.client);
	}
	
	List<TurboIssue> getAllIssues(IRepositoryIdProvider repository) {
		List<TurboIssue> turboIssues = new ArrayList<TurboIssue>();
		Map<String, String> filters = new HashMap<String, String>();
		filters.put(IssueService.FIELD_FILTER, "all");
		filters.put(IssueService.FILTER_STATE, "all");
		try {		
			List<Issue> issues = service.getIssues(repository, filters);
			for (Issue issue : issues) {
				turboIssues.add(new TurboIssue(issue));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return turboIssues;
	}
	
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

}
