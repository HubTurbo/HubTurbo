package backend.interfaces;

import java.util.concurrent.CompletableFuture;

public abstract class RepoTask<R, I, L, M, U> implements Runnable {
	public final Repo<I, L, M, U> repo;
	public final CompletableFuture<R> response;
	public final TaskRunner taskRunner;

	public RepoTask(TaskRunner taskRunner, Repo<I, L, M, U> repo) {
		this.taskRunner = taskRunner;
		this.repo = repo;
		response = new CompletableFuture<>();
	}

	public abstract void run();
}
