package backend.interfaces;

import java.util.concurrent.CompletableFuture;

public abstract class RepoTask<R> implements Runnable {
	public final Repo repo;
	public final CompletableFuture<R> response;
	public final TaskRunner taskRunner;

	public RepoTask(TaskRunner taskRunner, Repo repo) {
		this.taskRunner = taskRunner;
		this.repo = repo;
		response = new CompletableFuture<>();
	}

	public abstract void run();
}
