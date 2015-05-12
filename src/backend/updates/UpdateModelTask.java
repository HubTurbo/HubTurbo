package backend.updates;

import backend.Model;
import backend.UpdateSignature;
import backend.interfaces.Repo;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class UpdateModelTask extends RepoTask<Model> {

	private final Model model;

	public UpdateModelTask(BlockingQueue<RepoTask<?>> tasks, Repo repo, Model model) {
		super(tasks, repo);
		this.model = model;
	}

	@Override
	public void update() {
		UpdateIssuesTask issuesTask = new UpdateIssuesTask(tasks, repo, model);
		tasks.add(issuesTask);

		try {
			UpdateIssuesTask.Result issuesResult = issuesTask.response.get();
			// TODO fill out the others, don't leave them as null
			UpdateSignature newSignature =
				new UpdateSignature(issuesResult.ETag, null, null, null, issuesResult.lastCheckTime);
			Model result = new Model(model.getRepoId(), newSignature).withIssues(issuesResult.issues);
			response.complete(result);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}
}
