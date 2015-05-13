package backend.interfaces;

import backend.Model;
import backend.UserCredentials;

import java.util.concurrent.*;

public abstract class RepoSource {

	private static final int POOL_SIZE = 2;

	protected final BlockingQueue<RepoTask<?, ?>> tasks = new LinkedBlockingQueue<>();
	private final ExecutorService pool = Executors.newFixedThreadPool(POOL_SIZE);

	protected void activateThreads() {
		for (int i=0; i<POOL_SIZE; i++) {
			pool.execute(this::handleTask);
		}
	}

	protected void handleTask() {
		try {
			// Perform action
			tasks.take().run();

			// Recurse
			pool.execute(this::handleTask);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected <R, I> RepoTask<R, I> addTask(RepoTask<R, I> task) {
		tasks.add(task);
		return task;
	}

	protected void execute(Runnable r) {
		pool.execute(r);
	}

	public abstract CompletableFuture<Boolean> login(UserCredentials credentials);
	public abstract CompletableFuture<Model> downloadRepository(String repoId);
	public abstract CompletableFuture<Model> updateModel(Model model);
}
