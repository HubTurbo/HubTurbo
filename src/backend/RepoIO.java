package backend;

import backend.github.GitHubSource;
import backend.interfaces.RepoStore;
import backend.interfaces.RepoSource;
import backend.json.JSONStore;
import backend.resource.Model;
import backend.resource.serialization.SerializableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;

public class RepoIO {

	private static final Logger logger = LogManager.getLogger(RepoIO.class.getName());

	private final RepoSource repoSource = new GitHubSource();
	private final RepoStore repoStore = new JSONStore();

	public CompletableFuture<Boolean> login(UserCredentials credentials) {
		return repoSource.login(credentials);
	}

	public CompletableFuture<Model> openRepository(String repoId) {
		if (repoStore.isRepoStored(repoId)) {
			return repoStore.loadRepository(repoId).thenCompose(this::updateModel);
		} else {
			return repoSource.downloadRepository(repoId).thenApply(model -> {
				repoStore.saveRepository(repoId, new SerializableModel(model));
				return model;
			});
		}
	}

	public CompletableFuture<Model> updateModel(Model model) {
		return repoSource.updateModel(model).thenApply(newModel -> {
			if (!model.equals(newModel)) {
				repoStore.saveRepository(newModel.getRepoId().generateId(), new SerializableModel(newModel));
			} else {
				logger.info("Nothing has changed in " + model.getRepoId().generateId() + "; not writing to store");
			}
			return model;
		});
	}
}
