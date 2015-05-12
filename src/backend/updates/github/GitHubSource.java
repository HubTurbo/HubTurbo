package backend.updates.github;

import backend.Model;
import backend.UserCredentials;
import backend.interfaces.Repo;
import backend.interfaces.RepoSource;
import org.eclipse.egit.github.core.Issue;

import java.util.concurrent.*;

public class GitHubSource extends RepoSource {

	private final Repo<Issue> gitHub = new GitHubRepo();

	@Override
	public CompletableFuture<Boolean> login(UserCredentials credentials) {
		CompletableFuture<Boolean> response = new CompletableFuture<>();
		pool.execute(() -> response.complete(gitHub.login(credentials)));

		busyThreads();

		return response;
	}

	@Override
	public CompletableFuture<Model> downloadRepository(String repoId) {
		GHDownloadTask task = new GHDownloadTask(tasks, gitHub, repoId);
		tasks.add(task);
		return task.response;
	}

	@Override
	public CompletableFuture<Model> updateModel(Model model) {
		GHUpdateModelTask task = new GHUpdateModelTask(tasks, gitHub, model);
		tasks.add(task);
		return task.response;
	}
}
