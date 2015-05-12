package backend.updates;

import backend.interfaces.Repo;
import org.eclipse.egit.github.core.Issue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;

public abstract class RepoTask<R> {
	public final BlockingQueue<RepoTask<?>> tasks;
	public final Repo repo;
	public final CompletableFuture<R> response;

	public RepoTask(BlockingQueue<RepoTask<?>> tasks, Repo repo) {
		this.tasks = tasks;
		this.repo = repo;
		response = new CompletableFuture<>();
	}

	public abstract void update();
}
