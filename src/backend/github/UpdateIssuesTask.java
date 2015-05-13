package backend.github;

import backend.Model;
import backend.TurboIssue;
import backend.interfaces.Repo;
import backend.interfaces.RepoTask;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.eclipse.egit.github.core.Issue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;

class UpdateIssuesTask extends GitHubRepoTask<UpdateIssuesTask.Result> {

	private final Model model;

	public UpdateIssuesTask(BlockingQueue<RepoTask<?, ?>> tasks, Repo<Issue> repo, Model model) {
		super(tasks, repo);
		this.model = model;
	}

	@Override
	public void run() {
		ImmutableTriple<List<Issue>, String, Date> changes = repo.getUpdatedIssues(model.getRepoId().generateId(),
			model.getUpdateSignature().issuesETag, model.getUpdateSignature().lastCheckTime);

		// Reconcile changes
		List<TurboIssue> existing = model.getIssues();
		System.out.println("existing " + existing);
		List<Issue> changed = changes.left;
		System.out.println("changed " + changed);
		List<TurboIssue> updated = reconcile(existing, changed);
		System.out.println("updated " + updated);

		response.complete(new Result(updated, changes.middle, changes.right));
	}

	private List<TurboIssue> reconcile(List<TurboIssue> existing, List<Issue> changed) {
		existing = new ArrayList<>(existing);
		for (Issue issue : changed) {
			int id = issue.getNumber();

			// TODO O(n^2)
			Optional<Integer> corresponding = findIssueWithId(existing, id);
			if (corresponding.isPresent()) {
				existing.set(corresponding.get(), new TurboIssue(issue));
			} else {
				existing.add(new TurboIssue(issue));
			}
		}
		return existing;
	}

	private Optional<Integer> findIssueWithId(List<TurboIssue> existing, int id) {
		int i = 0;
		for (TurboIssue issue : existing) {
			if (issue.getId() == id) {
				return Optional.of(i);
			}
			++i;
		}
		return Optional.empty();
	}


	public static class Result {
		public final List<TurboIssue> issues;
		public final String ETag;
		public final Date lastCheckTime;

		public Result(List<TurboIssue> issues, String eTag, Date lastCheckTime) {
			this.issues = issues;
			ETag = eTag;
			this.lastCheckTime = lastCheckTime;
		}
	}
}
