package backend.interfaces;

import backend.resource.Model;
import backend.UserCredentials;

import java.util.concurrent.*;

public abstract class RepoSource implements TaskRunner {

	private final ExecutorService pool = Executors.newCachedThreadPool();

	@Override
	public <R, I, L, M, U> RepoTask<R, I, L, M, U> addTask(RepoTask<R, I, L, M, U> task) {
		execute(task);
		return task;
	}

	@Override
	public void execute(Runnable r) {
		pool.execute(r);
	}

	public abstract String getName();
	public abstract CompletableFuture<Boolean> login(UserCredentials credentials);
	public abstract CompletableFuture<Model> downloadRepository(String repoId);
	public abstract CompletableFuture<Model> updateModel(Model model);
}
