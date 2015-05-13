package backend.github;

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
		execute(() -> response.complete(gitHub.login(credentials)));

		activateThreads();

		return response;
	}

	@Override
	public CompletableFuture<Model> downloadRepository(String repoId) {
		return addTask(new DownloadTask(tasks, gitHub, repoId)).response;
	}

	@Override
	public CompletableFuture<Model> updateModel(Model model) {
		return addTask(new UpdateModelTask(tasks, gitHub, model)).response;
	}
}
