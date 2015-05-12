package backend;

import backend.interfaces.Repo;
import backend.interfaces.RepoSource;
import backend.updates.DownloadTask;
import backend.updates.RepoTask;
import backend.updates.UpdateModelTask;

import java.util.concurrent.*;

public class GitHubSource implements RepoSource {

	private static final int POOL_SIZE = 2;

	private final Repo gitHub = new GitHubRepo();
	private final BlockingQueue<RepoTask<?>> tasks = new LinkedBlockingQueue<>();
	private final ExecutorService pool = Executors.newFixedThreadPool(POOL_SIZE);

	@Override
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

	public void handleTask() {
		try {
			// Perform action
			tasks.take().update();

			// Recurse
			pool.execute(this::handleTask);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public CompletableFuture<Model> downloadRepository(String repoId) {
		DownloadTask task = new DownloadTask(tasks, gitHub, repoId);
		tasks.add(task);
		return task.response;
	}

	@Override
	public CompletableFuture<Model> updateModel(Model model) {
		UpdateModelTask task = new UpdateModelTask(tasks, gitHub, model);
		tasks.add(task);
		return task.response;
	}
}
