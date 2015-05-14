package backend.interfaces;

import backend.resource.Model;
import backend.UserCredentials;

import java.util.concurrent.*;

public abstract class RepoSource implements TaskRunner {

	private final ExecutorService pool = Executors.newCachedThreadPool();

	@Override
	public <R, I> RepoTask<R, I> addTask(RepoTask<R, I> task) {
		execute(task);
		return task;
	}

	@Override
	public void execute(Runnable r) {
		pool.execute(r);
	}

	public abstract CompletableFuture<Boolean> login(UserCredentials credentials);
	public abstract CompletableFuture<Model> downloadRepository(String repoId);
	public abstract CompletableFuture<Model> updateModel(Model model);
}
