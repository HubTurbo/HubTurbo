package backend.json;

import backend.interfaces.RepoStore;
import backend.resource.Model;
import backend.resource.serialization.SerializableModel;

import java.util.concurrent.CompletableFuture;

public class JSONStore extends RepoStore {

	@Override
	public CompletableFuture<Model> loadRepository(String repoId) {
		CompletableFuture<Model> response = new CompletableFuture<>();
		addTask(new ReadTask(repoId, response));
		return response;
	}

	@Override
	public void saveRepository(String repoId, SerializableModel model) {
		addTask(new WriteTask(repoId, model));
	}
}
