package backend.github;

import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.Logger;

import backend.UpdateSignature;
import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import backend.resource.*;
import util.HTLog;

public class UpdateModelTask extends GitHubRepoTask<Model> {

	private static final Logger logger = HTLog.get(UpdateModelTask.class);

	private final Model model;

	public UpdateModelTask(TaskRunner taskRunner, Repo repo, Model model) {
		super(taskRunner, repo);
		this.model = model;
	}

	@Override
	public void run() {
		UpdateIssuesTask issuesTask = new UpdateIssuesTask(taskRunner, repo, model);
		UpdateLabelsTask labelsTask = new UpdateLabelsTask(taskRunner, repo, model);
		UpdateMilestonesTask milestonesTask = new UpdateMilestonesTask(taskRunner, repo, model);
		UpdateUsersTask usersTask = new UpdateUsersTask(taskRunner, repo, model);

		taskRunner.execute(issuesTask);
		taskRunner.execute(labelsTask);
		taskRunner.execute(milestonesTask);
		taskRunner.execute(usersTask);

		try {
			GitHubRepoTask.Result<TurboIssue> issuesResult = issuesTask.response.get();
			GitHubRepoTask.Result<TurboLabel> labelsResult = labelsTask.response.get();
			GitHubRepoTask.Result<TurboMilestone> milestonesResult = milestonesTask.response.get();
			GitHubRepoTask.Result<TurboUser> usersResult = usersTask.response.get();

			UpdateSignature newSignature =
				new UpdateSignature(issuesResult.eTag, labelsResult.eTag,
					milestonesResult.eTag, usersResult.eTag, issuesResult.lastCheckTime);

			Model result = new Model(model.getRepoId(), issuesResult.items,
				labelsResult.items, milestonesResult.items, usersResult.items, newSignature);

			logger.info(HTLog.format(model.getRepoId(), "Updated model with " + result.summarise()));
			response.complete(result);
		} catch (InterruptedException | ExecutionException e) {
			HTLog.error(logger, e);
		}
	}
}
