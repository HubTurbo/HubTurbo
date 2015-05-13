package backend.github;

import backend.UserCredentials;
import backend.interfaces.Repo;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.NoSuchPageException;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.client.PagedRequest;
import org.eclipse.egit.github.core.service.IssueService;
import service.GitHubClientExtended;
import service.updateservice.IssueUpdateService;

import java.io.IOException;
import java.util.*;

public class GitHubRepo implements Repo<Issue> {

	private final GitHubClientExtended client = new GitHubClientExtended();
	//	private final ExecutorService pool = Executors.newFixedThreadPool(1);
	private final IssueService issueService = new IssueService();
//	private final IssueUpdateService issueUpdateService = new IssueUpdateService();

	public GitHubRepo() {
//		pool.execute(this::login);
//		login();
	}

	@Override
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

	@Override
	public ImmutableTriple<List<Issue>, String, Date> getUpdatedIssues(String repoId, String ETag, Date lastCheckTime) {
		IssueUpdateService issueUpdateService = new IssueUpdateService(client, ETag, lastCheckTime);
		return new ImmutableTriple<>(issueUpdateService.getUpdatedItems(RepositoryId.createFromId(repoId)),
			issueUpdateService.getUpdatedETag(), issueUpdateService.getUpdatedCheckTime());
	}

	@Override
	public List<Issue> getIssues(String repoId) {
		Map<String, String> filters = new HashMap<>();
		filters.put(IssueService.FIELD_FILTER, "all");
		filters.put(IssueService.FILTER_STATE, "all");
		return getAll(issueService.pageIssues(RepositoryId.createFromId(repoId), filters));
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
