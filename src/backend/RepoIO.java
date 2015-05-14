package backend;

import backend.interfaces.RepoCache;
import backend.interfaces.RepoSource;
import backend.github.GitHubSource;
import backend.json.JSONCache;
import backend.resource.Model;
import backend.resource.serialization.SerializableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;

public class RepoIO {

	private final RepoSource repoSource = new GitHubSource();
	private final RepoCache repoCache = new JSONCache();

	public CompletableFuture<Boolean> login(UserCredentials credentials) {
		return repoSource.login(credentials);
	}

	public CompletableFuture<Model> openRepository(String repoId) {
		if (repoCache.isRepoCached(repoId)) {
			return repoCache.loadRepository(repoId);
		} else {
			return repoSource.downloadRepository(repoId).thenApply(model -> {
				repoCache.saveRepository(repoId, new SerializableModel(model));
				return model;
			});
		}
	}

	public CompletableFuture<Model> updateModel(Model model) {
		return repoSource.updateModel(model);
	}
}
