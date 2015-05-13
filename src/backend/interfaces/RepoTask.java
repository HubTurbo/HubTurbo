package backend.interfaces;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;

public abstract class RepoTask<R, I> implements Runnable {
	public final BlockingQueue<RepoTask<?, ?>> tasks;
	public final Repo<I> repo;
	public final CompletableFuture<R> response;

	public RepoTask(BlockingQueue<RepoTask<?, ?>> tasks, Repo<I> repo) {
		this.tasks = tasks;
		this.repo = repo;
		response = new CompletableFuture<>();
	}

	public abstract void run();
}
