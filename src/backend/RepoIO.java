package backend;

import backend.github.GitHubSource;
import backend.interfaces.RepoSource;
import backend.interfaces.RepoStore;
import backend.json.JSONStore;
import backend.resource.Model;
import backend.resource.serialization.SerializableModel;
import org.apache.logging.log4j.Logger;
import util.HTLog;

import java.util.concurrent.CompletableFuture;

public class RepoIO {

	private static final Logger logger = HTLog.get(RepoIO.class);

	private final RepoSource repoSource = new GitHubSource();
	private final RepoStore repoStore = new JSONStore();

	public CompletableFuture<Boolean> login(UserCredentials credentials) {
		return repoSource.login(credentials);
	}

	public CompletableFuture<Model> openRepository(String repoId) {
		if (repoStore.isRepoStored(repoId)) {
			return repoStore.loadRepository(repoId).thenCompose(this::updateModel);
		} else {
			return repoSource.downloadRepository(repoId).thenCompose(this::updateModel);
		}
	}

	public CompletableFuture<Model> updateModel(Model model) {
		return repoSource.updateModel(model).thenApply(newModel -> {
			if (!model.equals(newModel)) {
				repoStore.saveRepository(newModel.getRepoId().generateId(), new SerializableModel(newModel));
			} else {
				logger.info(HTLog.format(model.getRepoId(), "Nothing changed; not writing to store"));
			}
			return model;
		});
	}
}
