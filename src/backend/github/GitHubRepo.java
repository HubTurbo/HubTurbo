package backend.github;

import backend.UserCredentials;
import backend.interfaces.Repo;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.*;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.NoSuchPageException;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.client.PagedRequest;
import org.eclipse.egit.github.core.service.*;
import service.GitHubClientExtended;
import service.IssueServiceExtended;
import service.LabelServiceFixed;
import service.updateservice.*;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;

public class GitHubRepo implements Repo<Issue, Label, Milestone, User> {

	private static final Logger logger = LogManager.getLogger(GitHubRepo.class.getName());

	private final GitHubClientExtended client = new GitHubClientExtended();
	private final IssueServiceExtended issueService = new IssueServiceExtended(client);
	private final CollaboratorService collaboratorService = new CollaboratorService(client);
	private final LabelServiceFixed labelService = new LabelServiceFixed(client);
	final private MilestoneService milestoneService = new MilestoneService(client);

	public GitHubRepo() {
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
			return false;
		}
		return true;
	}

	@Override
	public ImmutableTriple<List<Issue>, String, Date> getUpdatedIssues(String repoId, String ETag, Date lastCheckTime) {
		IssueUpdateService issueUpdateService = new IssueUpdateService(client, ETag, lastCheckTime);
		return new ImmutableTriple<>(issueUpdateService.getUpdatedItems(RepositoryId.createFromId(repoId)),
			issueUpdateService.getUpdatedETag(), issueUpdateService.getUpdatedCheckTime());
	}

	@Override
	public ImmutablePair<List<Label>, String> getUpdatedLabels(String repoId, String ETag) {
		return getUpdatedResource(repoId, ETag, LabelUpdateService::new);
	}

	@Override
	public ImmutablePair<List<Milestone>, String> getUpdatedMilestones(String repoId, String ETag) {
		return getUpdatedResource(repoId, ETag, MilestoneUpdateService::new);
	}

	@Override
	public ImmutablePair<List<User>, String> getUpdatedCollaborators(String repoId, String ETag) {
		return getUpdatedResource(repoId, ETag, UserUpdateService::new);
	}

	private <R, S extends UpdateService<R>> ImmutablePair<List<R>, String> getUpdatedResource(
		String repoId, String ETag, BiFunction<GitHubClientExtended, String, S> construct) {
		S updateService = construct.apply(client, ETag);
		return new ImmutablePair<>(updateService.getUpdatedItems(RepositoryId.createFromId(repoId)),
			updateService.getUpdatedETag());
	}

	@Override
	public List<Issue> getIssues(String repoId) {
		Map<String, String> filters = new HashMap<>();
		filters.put(IssueService.FIELD_FILTER, "all");
		filters.put(IssueService.FILTER_STATE, "all");
		return getAll(issueService.pageIssues(RepositoryId.createFromId(repoId), filters));
	}

	@Override
	public List<Label> getLabels(String repoId) {
		try {
			return labelService.getLabels(RepositoryId.createFromId(repoId));
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
			return new ArrayList<>();
		}
	}

	@Override
	public List<Milestone> getMilestones(String repoId) {
		try {
			return milestoneService.getMilestones(RepositoryId.createFromId(repoId), "all");
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
			return new ArrayList<>();
		}
	}

	@Override
	public List<User> getCollaborators(String repoId) {
		try {
			return collaboratorService.getCollaborators(RepositoryId.createFromId(repoId));
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
			return new ArrayList<>();
		}
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

