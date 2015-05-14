package backend;

import backend.github.GitHubSource;
import backend.interfaces.RepoCache;
import backend.interfaces.RepoSource;
import backend.json.JSONCache;
import backend.resource.Model;
import backend.resource.serialization.SerializableModel;

import java.util.concurrent.CompletableFuture;

public class RepoIO {

	private final RepoSource repoSource = new GitHubSource();
	private final RepoCache repoCache = new JSONCache();

	public CompletableFuture<Boolean> login(UserCredentials credentials) {
		return repoSource.login(credentials);
	}

	public CompletableFuture<Model> openRepository(String repoId) {
		if (repoCache.isRepoCached(repoId)) {
			return repoCache.loadRepository(repoId).thenCompose(this::updateModel);
		} else {
			return repoSource.downloadRepository(repoId).thenApply(model -> {
				repoCache.saveRepository(repoId, new SerializableModel(model));
				return model;
			});
		}
	}

	public CompletableFuture<Model> updateModel(Model model) {
		return repoSource.updateModel(model).thenApply(newModel -> {
			repoCache.saveRepository(newModel.getRepoId().generateId(), new SerializableModel(newModel));
			return model;
		});
	}
}
