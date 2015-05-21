package backend.github;

import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import backend.resource.Model;
import backend.resource.TurboMilestone;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.User;
import util.HTLog;
import util.Utility;

import java.util.List;
import java.util.stream.Collectors;

public class UpdateMilestonesTask extends GitHubRepoTask<GitHubRepoTask.Result> {

	private static final Logger logger = HTLog.get(UpdateMilestonesTask.class);

	private final Model model;

	public UpdateMilestonesTask(TaskRunner taskRunner, Repo<Issue, Label, Milestone, User> repo, Model model) {
		super(taskRunner, repo);
		this.model = model;
	}

	@Override
	public void run() {
		ImmutablePair<List<Milestone>, String> changes = repo.getUpdatedMilestones(model.getRepoId().generateId(),
			model.getUpdateSignature().milestonesETag);

		List<TurboMilestone> existing = model.getMilestones();
		List<Milestone> changed = changes.left;
		logger.info(HTLog.format(model.getRepoId(), "%s milestone(s)) changed%s",
			changed.size(), changed.size() == 0 ? "" : ": " + stringify(changed)));

		List<TurboMilestone> updated = Utility.reconcile(existing, changed,
			TurboMilestone::getTitle, Milestone::getTitle,
			m -> new TurboMilestone(model.getRepoId().generateId(), m));

		response.complete(new Result<>(updated, changes.right));
	}

	private String stringify(List<Milestone> milestones) {
		return "[" + milestones.stream()
			.map(Milestone::getTitle)
			.collect(Collectors.joining(", ")) + "]";
	}

}
