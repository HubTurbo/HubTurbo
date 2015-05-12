package backend;

import backend.interfaces.RepoSource;
import org.eclipse.egit.github.core.RepositoryId;

import javax.security.auth.login.CredentialException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class GitHubSource implements RepoSource  {

	private static final int POOL_SIZE = 2;

	private final GitHub gitHub = new GitHub();
	private final LinkedBlockingQueue<Task> tasks = new LinkedBlockingQueue<>();
	private final ExecutorService pool = Executors.newFixedThreadPool(POOL_SIZE);

	public CompletableFuture<Boolean> login(UserCredentials credentials) {
		CompletableFuture<Boolean> response = new CompletableFuture<>();
		pool.execute(() -> response.complete(gitHub.login(credentials)));

		init();

		return response;
	}

	private void init() {
		for (int i=0; i<POOL_SIZE; i++) {
			pool.execute(this::handleTask);
		}
	}

	@Override
	public CompletableFuture<Model> downloadRepository(String repoName) {
		CompletableFuture<Model> response = new CompletableFuture<>();
		tasks.add(new Task(repoName, response));
		return response;
	}

	public void handleTask() {
		try {

			Task task = tasks.take();
			List<TurboIssue> issues = gitHub.getIssues(task.repoName).stream()
				.map(TurboIssue::new)
				.collect(Collectors.toList());
			task.response.complete(new Model(RepositoryId.createFromId(task.repoName), issues, UpdateSignature.empty));
//			UI.instance.log("finished downloading issues from " + task.repoName);

			// Recurse
			pool.execute(this::handleTask);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private class Task {
		public final CompletableFuture<Model> response;
		public final String repoName;

		public Task(String repoName, CompletableFuture<Model> response) {
			this.repoName = repoName;
			this.response = response;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Task task = (Task) o;

			if (repoName != null ? !repoName.equals(task.repoName) : task.repoName != null) return false;
			if (response != null ? !response.equals(task.response) : task.response != null) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = response != null ? response.hashCode() : 0;
			result = 31 * result + (repoName != null ? repoName.hashCode() : 0);
			return result;
		}
	}
}
