package service;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_EVENTS;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_ISSUES;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.IssueEvent;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.GitHubResponse;
import org.eclipse.egit.github.core.service.IssueService;

public class IssueServiceExtended extends IssueService{
	private GitHubClientExtended ghClient;
	
	public static final String ISSUE_DATE = "date";
	public static final String ISSUE_CONTENTS = "issue";
	
	public IssueServiceExtended(GitHubClientExtended client){
		super(client);
		this.ghClient = client;
	}
	
	public HashMap<String, Object> getIssueData(IRepositoryIdProvider repository, int issueId) throws IOException{
		HashMap<String, Object> result = new HashMap<String, Object>();
		GitHubResponse response = getIssueResponse(repository.generateId(), Integer.toString(issueId));
		String dateModified = response.getHeader("Date");
		result.put(ISSUE_DATE, dateModified);
		result.put(ISSUE_CONTENTS, (Issue) response.getBody());
		return result;
	}
	
	private GitHubResponse getIssueResponse(String repoId, String issueNumber)
			throws IOException {
		if (issueNumber == null)
			throw new IllegalArgumentException("Issue number cannot be null"); //$NON-NLS-1$
		if (issueNumber.length() == 0)
			throw new IllegalArgumentException("Issue number cannot be empty"); //$NON-NLS-1$

		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(repoId);
		uri.append(SEGMENT_ISSUES);
		uri.append('/').append(issueNumber);
		GitHubRequest request = createRequest();
		request.setUri(uri);
		request.setType(Issue.class);
		return ghClient.get(request);
	}
	
	private HttpURLConnection createIssuePostConnection(IRepositoryIdProvider repository, int issueId) throws IOException{
		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(repository.generateId());
		uri.append(SEGMENT_ISSUES);
		uri.append('/').append(issueId);
		HttpURLConnection connection = ghClient.createPost(uri.toString());
		return connection;
	}
	
	@Override
	public Issue createIssue(IRepositoryIdProvider repository, Issue issue) throws IOException{
		Issue returnedIssue = super.createIssue(repository, issue);
		if(returnedIssue.getState() != issue.getState()){
			returnedIssue.setState(issue.getState());
			editIssueState(repository, returnedIssue.getNumber(), returnedIssue.getState().equals(STATE_OPEN));
		}
		return returnedIssue;
	}
	
	public Issue editIssue(IRepositoryIdProvider repository, Issue issue, String dateModified) throws IOException {
		if (issue == null)
			throw new IllegalArgumentException("Issue cannot be null"); //$NON-NLS-1$

		HttpURLConnection connection = createIssuePostConnection(repository, issue.getNumber());
		connection.setRequestProperty("If-Unmodified-Since", dateModified);
		Map<Object, Object> params = createIssueMap(issue, false);
		String state = issue.getState();
		if (state != null)
			params.put(FILTER_STATE, state);
		return ghClient.sendJson(connection, params, Issue.class);
	}
	
	public Issue editIssueTitle(IRepositoryIdProvider repository, int issueId, String title) throws IOException{
		HttpURLConnection connection = createIssuePostConnection(repository, issueId);
		HashMap<Object, Object> data = new HashMap<Object, Object>();
		data.put(FIELD_TITLE, title);
		return ghClient.sendJson(connection, data, Issue.class);
	}
	
	public Issue editIssueBody(IRepositoryIdProvider repository, int issueId, String body) throws IOException{
		HttpURLConnection connection = createIssuePostConnection(repository, issueId);
		HashMap<Object, Object> data = new HashMap<Object, Object>();
		data.put(FIELD_BODY, body);
		return ghClient.sendJson(connection, data, Issue.class);
	}
	
	public Issue editIssueState(IRepositoryIdProvider repository, int issueId, boolean open) throws IOException{
		HttpURLConnection connection = createIssuePostConnection(repository, issueId);
		HashMap<Object, Object> data = new HashMap<Object, Object>();
		String state;
		if(open){
			state = STATE_OPEN;
		}else{
			state = STATE_CLOSED;
		}
		data.put(FILTER_STATE, state);
		return ghClient.sendJson(connection, data, Issue.class);
	}
	
	public Issue setIssueMilestone(IRepositoryIdProvider repository, int issueId, Milestone milestone) throws IOException{
		HttpURLConnection connection = createIssuePostConnection(repository, issueId);
		HashMap<Object, Object> data = new HashMap<Object, Object>();
		if (milestone != null && milestone.getNumber() > 0) {
			data.put(FILTER_MILESTONE, Integer.toString(milestone.getNumber()));
		}else{
			data.put(FILTER_MILESTONE, ""); 
		}
		return ghClient.sendJson(connection, data, Issue.class);
	}
	
	public Issue setIssueAssignee(IRepositoryIdProvider repository, int issueId, User user) throws IOException{
		HttpURLConnection connection = createIssuePostConnection(repository, issueId);
		HashMap<Object, Object> data = new HashMap<Object, Object>();
		if(user != null && user.getLogin() != null){
			data.put(FILTER_ASSIGNEE, user.getLogin());
		}else{
			data.put(FILTER_ASSIGNEE, "");
		}
		return ghClient.sendJson(connection, data, Issue.class);
	}
	
	/**
	 * Retrieves a list of all issue events.
	 * @param user
	 * @param repository
	 * @return list of issue events
	 * @throws IOException
	 */
	public GitHubEventsResponse getIssueEvents(IRepositoryIdProvider repository, int issueId) throws IOException {
		GitHubRequest request = createRequest();
		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(repository.generateId());
		uri.append(SEGMENT_ISSUES);
		uri.append('/').append(issueId);
		uri.append(SEGMENT_EVENTS);
		request.setUri(uri);
		request.setType(IssueEvent[].class);
		GitHubEventsResponse response = ghClient.getEvent(request);
		return response;
	}
}
