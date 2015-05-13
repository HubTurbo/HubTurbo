package backend.interfaces;

import backend.Model;
import backend.SerializableModel;
import backend.json.CacheTask;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class RepoCache {

	private final LinkedBlockingQueue<CacheTask> tasks = new LinkedBlockingQueue<>();
	private final ExecutorService pool = Executors.newFixedThreadPool(1);

	public void init() {
		pool.execute(this::handleTask);
	}

	public void handleTask() {
		try {
			tasks.take().update();
			// Recurse
			pool.execute(this::handleTask);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static String escapeRepoName(String repoName) {
		return repoName.replace("/", "-") + ".json";
	}

	protected void addTask(CacheTask task) {
		tasks.add(task);
	}

	public abstract boolean isRepoCached(String repoId);
	public abstract CompletableFuture<Model> loadRepository(String repoId);
	public abstract void saveRepository(String repoId, SerializableModel model);
}
