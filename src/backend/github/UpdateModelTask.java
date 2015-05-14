package backend.github;

import backend.resource.Model;
import backend.UpdateSignature;
import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.Issue;

import java.util.concurrent.ExecutionException;

public class UpdateModelTask extends GitHubRepoTask<Model> {

	private static final Logger logger = LogManager.getLogger(UpdateModelTask.class.getName());

	private final Model model;

	public UpdateModelTask(TaskRunner taskRunner, Repo<Issue> repo, Model model) {
		super(taskRunner, repo);
		this.model = model;
	}

	@Override
	public void run() {
		UpdateIssuesTask issuesTask = new UpdateIssuesTask(taskRunner, repo, model);
		taskRunner.execute(issuesTask);

		try {
			UpdateIssuesTask.Result issuesResult = issuesTask.response.get();
			// TODO fill out the others, don't leave them as null
			UpdateSignature newSignature =
				new UpdateSignature(issuesResult.ETag, null, null, null, issuesResult.lastCheckTime);
			Model result = new Model(model.getRepoId(), newSignature).withIssues(issuesResult.issues);
			logger.info("Updated model with " + result.summarise());
			response.complete(result);
		} catch (InterruptedException | ExecutionException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}
}
