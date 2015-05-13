package backend.interfaces;

import backend.Model;
import backend.SerializableModel;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class RepoCache {

	private final ExecutorService pool = Executors.newSingleThreadExecutor();

	public static String escapeRepoName(String repoName) {
		return repoName.replace("/", "-") + ".json";
	}

	protected void addTask(CacheTask task) {
		pool.execute(task);
	}

	public abstract boolean isRepoCached(String repoId);
	public abstract CompletableFuture<Model> loadRepository(String repoId);
	public abstract void saveRepository(String repoId, SerializableModel model);
}
