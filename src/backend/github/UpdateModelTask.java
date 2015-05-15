package backend.github;

import backend.UpdateSignature;
import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import backend.resource.Model;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.User;
import util.HTLog;

import java.util.concurrent.ExecutionException;

public class UpdateModelTask extends GitHubRepoTask<Model> {

	private static final Logger logger = HTLog.get(UpdateModelTask.class);

	private final Model model;

	public UpdateModelTask(TaskRunner taskRunner, Repo<Issue, Label, Milestone, User> repo, Model model) {
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
			GitHubRepoTask.Result issuesResult = issuesTask.response.get();
			GitHubRepoTask.Result labelsResult = labelsTask.response.get();
			GitHubRepoTask.Result milestonesResult = milestonesTask.response.get();
			GitHubRepoTask.Result usersResult = usersTask.response.get();

			UpdateSignature newSignature =
				new UpdateSignature(issuesResult.ETag, labelsResult.ETag,
					milestonesResult.ETag, usersResult.ETag, issuesResult.lastCheckTime);

			Model result = new Model(model.getRepoId(), model.getIssues(),
				model.getLabels(), model.getMilestones(), model.getUsers(), newSignature);

			logger.info(HTLog.format(model.getRepoId(), "Updated model with " + result.summarise()));
			response.complete(result);
		} catch (InterruptedException | ExecutionException e) {
			HTLog.error(logger, e);
		}
	}
}
