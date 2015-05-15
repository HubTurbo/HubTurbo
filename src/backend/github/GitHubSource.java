package backend.github;

import backend.resource.Model;
import backend.UserCredentials;
import backend.interfaces.Repo;
import backend.interfaces.RepoSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.Issue;

import java.util.concurrent.CompletableFuture;

public class GitHubSource extends RepoSource {

	private static final Logger logger = LogManager.getLogger(GitHubSource.class.getName());

	private final Repo<Issue> gitHub = new GitHubRepo();

	@Override
	public String getName() {
		return "GitHub";
	}

	@Override
	public CompletableFuture<Boolean> login(UserCredentials credentials) {
		CompletableFuture<Boolean> response = new CompletableFuture<>();
		execute(() -> {
			boolean success = gitHub.login(credentials);
			logger.info(String.format("%s to %s as %s",
				success ? "Logged in" : "Failed to log in",
				getName(), credentials.username));
			response.complete(success);
		});

		return response;
	}

	@Override
	public CompletableFuture<Model> downloadRepository(String repoId) {
		return addTask(new DownloadRepoTask(this, gitHub, repoId)).response;
	}

	@Override
	public CompletableFuture<Model> updateModel(Model model) {
		return addTask(new UpdateModelTask(this, gitHub, model)).response;
	}
}
