package backend.github;

import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import backend.resource.Model;
import backend.resource.TurboIssue;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.User;
import util.HTLog;

import java.util.Date;
import java.util.List;

public class UpdateIssuesTask extends GitHubRepoTask<GitHubRepoTask.Result<TurboIssue>> {

	private static final Logger logger = HTLog.get(UpdateIssuesTask.class);

	private final Model model;

	public UpdateIssuesTask(TaskRunner taskRunner, Repo<Issue, Label, Milestone, User> repo, Model model) {
		super(taskRunner, repo);
		this.model = model;
	}

	@Override
	public void run() {
		ImmutableTriple<List<Issue>, String, Date> changes = repo.getUpdatedIssues(model.getRepoId().generateId(),
			model.getUpdateSignature().issuesETag, model.getUpdateSignature().lastCheckTime);

		List<TurboIssue> existing = model.getIssues();
		List<Issue> changed = changes.left;
		logger.info(HTLog.format(model.getRepoId(), "%s issue(s)) changed%s",
			changed.size(), changed.size() == 0 ? "" : ": " + changed));

		List<TurboIssue> updated = TurboIssue.reconcile(model.getRepoId().generateId(), existing, changed);

		response.complete(new Result<>(updated, changes.middle, changes.right));
	}
}
