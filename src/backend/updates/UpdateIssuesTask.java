package backend.updates;

import backend.Model;
import backend.TurboIssue;
import backend.interfaces.Repo;
import backend.updates.github.GHRepoTask;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.eclipse.egit.github.core.Issue;

import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class UpdateIssuesTask extends GHRepoTask<UpdateIssuesTask.Result> {

	private final Model model;

	public UpdateIssuesTask(BlockingQueue<RepoTask<?, ?>> tasks, Repo repo, Model model) {
		super(tasks, repo);
		this.model = model;
	}

	@Override
	public void update() {
		ImmutableTriple<List<Issue>, String, Date> changes = repo.getUpdatedIssues(model.getRepoId().generateId(),
			model.getUpdateSignature().issuesETag, model.getUpdateSignature().lastCheckTime);
		// TODO reconcile changes
		response.complete(new Result(model.getIssues(), changes.middle, changes.right));
	}

//	public void updateCachedIssues(CompletableFuture<Integer> response, List<Issue> newIssues, String repoId) {
//
//		if (newIssues.size() == 0) {
//			assert false : "updateCachedIssues should not be called before issues have been loaded";
//			return;
//		}
//
//		run(() -> {
//			for (int i = newIssues.size() - 1; i >= 0; i--) {
//				Issue issue = newIssues.get(i);
//				TurboIssue newCached = new TurboIssue(issue, Model.this);
//				updateCachedIssue(newCached);
//			}
//			response.complete(newIssues.size());
//		});
//	}
//
//	/**
//	 * Given a TurboIssue, adds it to the model if it is not yet in it,
//	 * otherwise updates the corresponding issue in the model with its fields.
//	 *
//	 * @param issue
//	 */
//	public void updateCachedIssue(TurboIssue issue) {
//		TurboIssue tIssue = getIssueWithId(issue.getId());
//		if (tIssue != null) {
//			tIssue.copyValuesFrom(issue);
//			logger.info("Updated issue: " + issue.getId());
//		} else {
//			issues.add(0, issue);
//			logger.info("Added issue: " + issue.getId());
//		}
//	}

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
