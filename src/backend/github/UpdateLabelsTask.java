package backend.github;

import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import backend.resource.Model;
import backend.resource.TurboLabel;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.User;
import util.HTLog;
import util.Utility;

import java.util.List;

public class UpdateLabelsTask extends GitHubRepoTask<GitHubRepoTask.Result> {

	private static final Logger logger = HTLog.get(UpdateLabelsTask.class);

	private final Model model;

	public UpdateLabelsTask(TaskRunner taskRunner, Repo<Issue, Label, Milestone, User> repo, Model model) {
		super(taskRunner, repo);
		this.model = model;
	}

	@Override
	public void run() {
		ImmutablePair<List<Label>, String> changes = repo.getUpdatedLabels(model.getRepoId().generateId(),
			model.getUpdateSignature().labelsETag);

		List<TurboLabel> existing = model.getLabels();
		List<Label> changed = changes.left;
		logger.info(HTLog.format(model.getRepoId(), "%s label(s)) changed%s",
			changed.size(), changed.size() == 0 ? "" : ": " + changed));

		List<TurboLabel> updated = Utility.reconcile(existing, changed,
			TurboLabel::getName, Label::getName, l -> new TurboLabel(model.getRepoId().generateId(), l));

		response.complete(new Result<>(updated, changes.right));
	}
}
