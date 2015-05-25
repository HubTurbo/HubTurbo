package backend.github;

import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import backend.resource.Model;
import backend.resource.TurboUser;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.Logger;
import util.HTLog;

import java.util.ArrayList;
import java.util.List;

public class UpdateUsersTask extends GitHubRepoTask<GitHubRepoTask.Result<TurboUser>> {

	private static final Logger logger = HTLog.get(UpdateUsersTask.class);

	private final Model model;

	public UpdateUsersTask(TaskRunner taskRunner, Repo repo, Model model) {
		super(taskRunner, repo);
		this.model = model;
	}

	@Override
	public void run() {
		ImmutablePair<List<TurboUser>, String> changes = repo.getUpdatedCollaborators(model.getRepoId(),
			model.getUpdateSignature().collaboratorsETag);

		List<TurboUser> changed = changes.left;

		logger.info(HTLog.format(model.getRepoId(), "%s user(s)) changed%s",
			changed.size(), changed.isEmpty() ? "" : ": " + changed));

		List<TurboUser> updated = changed.isEmpty()
			? model.getUsers()
			: new ArrayList<>(changed);

		response.complete(new Result<>(updated, changes.right));
	}
}
