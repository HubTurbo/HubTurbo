package service;

import model.TurboIssue;
import model.TurboLabel;
import model.TurboMilestone;
import model.TurboUser;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.User;

import java.util.List;

/**
 * An aggregation of resources obtained from a repository. May be loaded from a cache or not,
 * in which case the types of the resources would be different. Use {@link #isCached()} to
 * check which case this falls into before using the getters.
 */
public class RepositoryResources {
	private List<Issue> issues;
	private List<Label> labels;
	private List<Milestone> milestones;
	private List<User> users;

	private List<TurboIssue> turboIssues;
	private List<TurboLabel> turboLabels;
	private List<TurboMilestone> turboMilestones;
	private List<TurboUser> turboUsers;

	private RepositoryResources() {}

	public static RepositoryResources fromGitHub(List<Issue> issues, List<Label> labels, List<Milestone> milestones, List<User> users) {
		RepositoryResources result = new RepositoryResources();

		result.issues = issues;
		result.labels = labels;
		result.milestones = milestones;
		result.users = users;

		result.turboIssues = null;
		result.turboLabels = null;
		result.turboMilestones = null;
		result.turboUsers = null;

		return result;
	}

	public static RepositoryResources fromCache(List<TurboIssue> turboIssues, List<TurboLabel> turboLabels,
	                                            List<TurboMilestone> turboMilestones, List<TurboUser> turboUsers) {
		RepositoryResources result = new RepositoryResources();

		result.issues = null;
		result.labels = null;
		result.milestones = null;
		result.users = null;

		result.turboIssues = turboIssues;
		result.turboLabels = turboLabels;
		result.turboMilestones = turboMilestones;
		result.turboUsers = turboUsers;

		return result;
	}

	public boolean isCached() {
		return issues == null && turboIssues != null;
	}

	private void assertCached() {
		assert isCached() : "This object contains only cached data. Check isCached()!";
	}

	private void assertNotCached() {
		assert !isCached() : "This object does not contain any cached data. Check isCached()!";
	}

	public List<Issue> getIssues() {
		assertNotCached();
		return issues;
	}

	public List<Label> getLabels() {
		assertNotCached();
		return labels;
	}

	public List<Milestone> getMilestones() {
		assertNotCached();
		return milestones;
	}

	public List<User> getUsers() {
		assertNotCached();
		return users;
	}

	public List<TurboIssue> getTurboIssues() {
		assertCached();
		return turboIssues;
	}

	public List<TurboLabel> getTurboLabels() {
		assertCached();
		return turboLabels;
	}

	public List<TurboMilestone> getTurboMilestones() {
		assertCached();
		return turboMilestones;
	}

	public List<TurboUser> getTurboUsers() {
		assertCached();
		return turboUsers;
	}
}
