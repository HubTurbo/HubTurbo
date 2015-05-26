package backend.github;

import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import backend.resource.Model;
import backend.resource.TurboIssue;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.logging.log4j.Logger;
import util.HTLog;

import java.util.Date;
import java.util.List;

public class UpdateIssuesTask extends GitHubRepoTask<GitHubRepoTask.Result<TurboIssue>> {

	private static final Logger logger = HTLog.get(UpdateIssuesTask.class);

	private final Model model;

	public UpdateIssuesTask(TaskRunner taskRunner, Repo repo, Model model) {
		super(taskRunner, repo);
		this.model = model;
	}

	@Override
	public void run() {
		ImmutableTriple<List<TurboIssue>, String, Date> changes = repo.getUpdatedIssues(model.getRepoId(),
			model.getUpdateSignature().issuesETag, model.getUpdateSignature().lastCheckTime);

		List<TurboIssue> existing = model.getIssues();
		List<TurboIssue> changed = changes.left;

		logger.info(HTLog.format(model.getRepoId(), "%s issue(s)) changed%s",
			changed.size(), changed.isEmpty() ? "" : ": " + changed));

		List<TurboIssue> updated = changed.isEmpty()
			? existing
			: TurboIssue.reconcile(model.getRepoId(), existing, changed);

		response.complete(new Result<>(updated, changes.middle, changes.right));
	}
}
