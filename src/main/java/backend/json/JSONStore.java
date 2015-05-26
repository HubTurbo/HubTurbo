package backend.json;

import backend.interfaces.RepoStore;
import backend.resource.Model;
import backend.resource.serialization.SerializableModel;

import java.util.concurrent.CompletableFuture;

public class JSONStore extends RepoStore {

	@Override
	public CompletableFuture<Model> loadRepository(String repoName) {
		CompletableFuture<Model> response = new CompletableFuture<>();
		addTask(new ReadTask(repoName, response));
		return response;
	}

	@Override
	public void saveRepository(String repoId, SerializableModel model) {
		addTask(new WriteTask(repoId, model));
	}
}
