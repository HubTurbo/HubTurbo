package backend;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.*;
import org.eclipse.egit.github.core.service.IssueService;
import service.GitHubClientExtended;

import java.io.IOException;
import java.util.*;

public class GitHub {

	private final GitHubClientExtended client = new GitHubClientExtended();
	//	private final ExecutorService pool = Executors.newFixedThreadPool(1);
	private final IssueService issueService = new IssueService();
//	private final IssueUpdateService issueUpdateService = new IssueUpdateService();

	public GitHub() {
//		pool.execute(this::login);
//		login();
	}

	public boolean login(UserCredentials credentials) {
		client.setCredentials(credentials.username, credentials.password);

		// Attempt login
		try {
			GitHubRequest request = new GitHubRequest();
			request.setUri("/");
			client.get(request);
		} catch (IOException e) {
			// Login failed
			e.printStackTrace();
			return false;
		}
		return true;
//		UI.instance.log("logged in");
//		UI.instance.events.post(new LoginEvent());
	}

	public List<Issue> getIssues(String repoName) {
		Map<String, String> filters = new HashMap<>();
		filters.put(IssueService.FIELD_FILTER, "all");
		filters.put(IssueService.FILTER_STATE, "all");
		return getAll(issueService.pageIssues(RepositoryId.createFromId(repoName), filters));
	}

	private List<Issue> getAll(PageIterator<Issue> iterator) {
		List<Issue> elements = new ArrayList<>();

		// Assume there is at least one page
		int knownLastPage = 1;

		try {
			while (iterator.hasNext()) {
				Collection<Issue> additions = iterator.next();
				elements.addAll(additions);

				// Compute progress

				// iterator.getLastPage() only has a value after iterator.next() is called,
				// so it's used directly in this loop. It returns the 1-based index of the last
				// page, except when we are actually on the last page, in which case it returns -1.
				// This portion deals with all these quirks.

				knownLastPage = Math.max(knownLastPage, iterator.getLastPage());
				int totalIssueCount = knownLastPage * PagedRequest.PAGE_SIZE;
				// Total is approximate: always >= the actual amount
				assert totalIssueCount >= elements.size();
			}
		} catch (NoSuchPageException pageException) {
			try {
				throw pageException.getCause();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return elements;
	}
}
