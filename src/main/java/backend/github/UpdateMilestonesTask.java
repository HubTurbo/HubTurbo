package backend.github;

import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import backend.resource.Model;
import backend.resource.TurboMilestone;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.Logger;
import util.HTLog;

import java.util.ArrayList;
import java.util.List;

public class UpdateMilestonesTask extends GitHubRepoTask<GitHubRepoTask.Result<TurboMilestone>> {

	private static final Logger logger = HTLog.get(UpdateMilestonesTask.class);

	private final Model model;

	public UpdateMilestonesTask(TaskRunner taskRunner, Repo repo, Model model) {
		super(taskRunner, repo);
		this.model = model;
	}

	@Override
	public void run() {
		ImmutablePair<List<TurboMilestone>, String> changes = repo.getUpdatedMilestones(model.getRepoId(),
			model.getUpdateSignature().milestonesETag);

		List<TurboMilestone> changed = changes.left;

		logger.info(HTLog.format(model.getRepoId(), "%s milestone(s)) changed%s",
			changed.size(), changed.isEmpty() ? "" : ": " + changed));

		List<TurboMilestone> updated = changed.isEmpty()
			? model.getMilestones()
			: new ArrayList<>(changed);

		response.complete(new Result<>(updated, changes.right));
	}
}
