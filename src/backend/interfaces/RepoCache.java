package backend.interfaces;

import backend.Model;
import backend.SerializableModel;

import java.util.concurrent.CompletableFuture;

public interface RepoCache {
	public boolean isRepoCached(String repoId);
	public CompletableFuture<Model> loadRepository(String repoId);
	public void saveRepository(String repoId, SerializableModel model);
}
