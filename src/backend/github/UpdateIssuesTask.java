package backend.github;

import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import backend.resource.Model;
import backend.resource.TurboIssue;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.User;
import util.Utility;

import java.util.Date;
import java.util.List;

public class UpdateIssuesTask extends GitHubRepoTask<GitHubRepoTask.Result> {

	private static final Logger logger = LogManager.getLogger(UpdateIssuesTask.class.getName());

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
		logger.info(changed.size() + " issue(s) changed" + (changed.size() == 0 ? "" : ": " + changed));

		List<TurboIssue> updated = Utility.reconcile(existing, changed,
			TurboIssue::getId, Issue::getNumber, TurboIssue::new);

		response.complete(new Result<>(updated, changes.middle, changes.right));
	}
}
