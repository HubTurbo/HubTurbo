package backend.github;

import backend.resource.Model;
import backend.resource.TurboIssue;
import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.Issue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class UpdateIssuesTask extends GitHubRepoTask<UpdateIssuesTask.Result> {

	private static final Logger logger = LogManager.getLogger(UpdateIssuesTask.class.getName());

	private final Model model;

	public UpdateIssuesTask(TaskRunner taskRunner, Repo<Issue> repo, Model model) {
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
