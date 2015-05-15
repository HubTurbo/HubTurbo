package backend.json;

import backend.resource.Model;
import backend.resource.serialization.SerializableModel;
import backend.interfaces.RepoStore;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class JSONStore extends RepoStore {

	@Override
	public boolean isRepoStored(String repoName) {
		File file = new File(escapeRepoName(repoName));
		return file.exists() && file.isFile();
	}

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
