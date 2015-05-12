package backend.updates.github;

import backend.Model;
import backend.UpdateSignature;
import backend.interfaces.Repo;
import backend.updates.RepoTask;
import org.eclipse.egit.github.core.Issue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;

public class GHUpdateModelTask extends GHRepoTask<Model> {

	private final Model model;

	public GHUpdateModelTask(BlockingQueue<RepoTask<?, ?>> tasks, Repo<Issue> repo, Model model) {
		super(tasks, repo);
		this.model = model;
	}

	@Override
	public void update() {
		GHUpdateIssuesTask issuesTask = new GHUpdateIssuesTask(tasks, repo, model);
		tasks.add(issuesTask);

		try {
			GHUpdateIssuesTask.Result issuesResult = issuesTask.response.get();
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
