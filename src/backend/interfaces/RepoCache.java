package backend.interfaces;

import backend.resource.Model;
import backend.resource.serialization.SerializableModel;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
