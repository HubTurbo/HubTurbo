package backend.github;

import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import backend.resource.Model;
import backend.resource.TurboLabel;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.Logger;
import util.HTLog;

import java.util.ArrayList;
import java.util.List;

public class UpdateLabelsTask extends GitHubRepoTask<GitHubRepoTask.Result<TurboLabel>> {

	private static final Logger logger = HTLog.get(UpdateLabelsTask.class);

	private final Model model;

	public UpdateLabelsTask(TaskRunner taskRunner, Repo repo, Model model) {
		super(taskRunner, repo);
		this.model = model;
	}

	@Override
	public void run() {
		ImmutablePair<List<TurboLabel>, String> changes = repo.getUpdatedLabels(model.getRepoId(),
			model.getUpdateSignature().labelsETag);

		List<TurboLabel> changed = changes.left;

		logger.info(HTLog.format(model.getRepoId(), "%s label(s)) changed%s",
			changed.size(), changed.isEmpty() ? "" : ": " + changed));

		List<TurboLabel> updated = changed.isEmpty()
			? model.getLabels()
			: new ArrayList<>(changed);

		response.complete(new Result<>(updated, changes.right));
	}
}
