package backend;

import backend.github.GitHubSource;
import backend.interfaces.RepoSource;
import backend.interfaces.RepoStore;
import backend.json.JSONStore;
import backend.json.JSONStoreStub;
import backend.resource.Model;
import backend.resource.serialization.SerializableModel;
import backend.stub.DummySource;
import org.apache.logging.log4j.Logger;
import ui.UI;
import util.HTLog;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static util.Futures.withResult;

public class RepoIO {

	private static final Logger logger = HTLog.get(RepoIO.class);

	private final RepoSource repoSource;
	private final RepoStore repoStore;

	public RepoIO(boolean isTestMode, boolean enableTestJSON) {
		if (isTestMode && !enableTestJSON) {
			repoStore = new JSONStoreStub();
		} else {
			repoStore = new JSONStore();
		}

		if (isTestMode) {
			repoSource = new DummySource();
			repoStore.enableTestDirectory();
		} else {
			repoSource = new GitHubSource();
		}
	}

	public CompletableFuture<Boolean> login(UserCredentials credentials) {
		return repoSource.login(credentials);
	}

	public CompletableFuture<Boolean> isRepositoryValid(String repoId) {
		return repoSource.isRepositoryValid(repoId);
	}

	public CompletableFuture<Model> openRepository(String repoId) {
		if (repoStore.isRepoStored(repoId)) {
			return repoStore.loadRepository(repoId)
				.thenCompose(this::updateModel)
				.exceptionally(withResult(new Model(repoId)));
		} else {
			return repoSource.downloadRepository(repoId)
				.thenCompose(this::updateModel)
				.exceptionally(withResult(new Model(repoId)));
		}
	}

	public CompletableFuture<Model> updateModel(Model model) {
		return repoSource.updateModel(model)
			.thenApply(newModel -> {
				UI.status.displayMessage(model.getRepoId() + " is up to date!");
				if (!model.equals(newModel)) {
					repoStore.saveRepository(newModel.getRepoId(), new SerializableModel(newModel));
				} else {
					logger.info(HTLog.format(model.getRepoId(), "Nothing changed; not writing to store"));
				}
				return newModel;
			}).exceptionally(withResult(new Model(model.getRepoId())));
	}

	public CompletableFuture<Map<Integer, IssueMetadata>> getIssueMetadata(String repoId, List<Integer> issues) {
		return repoSource.downloadMetadata(repoId, issues);
	}
}
