package backend;

import backend.resource.Model;
import backend.resource.MultiModel;
import org.apache.logging.log4j.Logger;
import prefs.Preferences;
import ui.UI;
import util.Futures;
import util.HTLog;
import util.Utility;
import util.events.RepoOpenedEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static util.Futures.withResult;

public class Logic {

	private static final Logger logger = HTLog.get(Logic.class);

	private final MultiModel models = new MultiModel();
	private final UIManager uiManager;
	private final Preferences prefs;

	private RepoIO repoIO;// = new RepoIO();

	// Assumed to be always present when app starts
	public UserCredentials credentials = null;

	public Logic(UIManager uiManager, Preferences prefs, boolean isTestMode) {
		this.uiManager = uiManager;
		this.prefs = prefs;

		repoIO = new RepoIO(isTestMode);

		// Pass the currently-empty model to the UI
		uiManager.updateNow(models);
	}

	private CompletableFuture<Boolean> isRepositoryValid(String repoId) {
		return repoIO.isRepositoryValid(repoId);
	}

	public CompletableFuture<Boolean> login(String username, String password) {
		String message = "Logging in as " + username;
		logger.info(message);
		UI.status.displayMessage(message);

		credentials = new UserCredentials(username, password);
		return repoIO.login(credentials);
	}

	public void refresh() {
		String message = "Refreshing " + models.toModels().stream()
			.map(Model::getRepoId)
			.collect(Collectors.joining(", "));

		logger.info(message);
		UI.status.displayMessage(message);

		Futures.sequence(models.toModels().stream()
			.map(repoIO::updateModel)
			.collect(Collectors.toList()))
			.thenApply(models::replace)
			.thenRun(this::updateUI)
			.exceptionally(Futures::log);
	}

	private void updateUI() {
		uiManager.update(models, true);
	}

	private void updateUIWithoutMetadata() {
		uiManager.update(models, false);
	}

	public CompletableFuture<Boolean> openRepository(String repoId) {
		assert Utility.isWellFormedRepoId(repoId);
		if (isAlreadyOpen(repoId) || models.isRepositoryPending(repoId)) {
			return Futures.unit(false);
		}
		models.addPendingRepository(repoId);
		return isRepositoryValid(repoId).thenCompose(valid -> {
			if (!valid) {
				return Futures.unit(false);
			} else {
				prefs.addToLastViewedRepositories(repoId);
				logger.info("Opening " + repoId);
				UI.status.displayMessage("Opening " + repoId);
				return repoIO.openRepository(repoId)
					.thenApply(models::addPending)
					.thenRun(this::updateUI)
					.thenRun(() -> UI.events.triggerEvent(new RepoOpenedEvent(repoId)))
					.thenApply(n -> true)
					.exceptionally(withResult(false));
			}
		});
	}

	public CompletableFuture<Map<Integer, IssueMetadata>> getIssueMetadata(String repoId, List<Integer> issues) {
		logger.info("Getting metadata for issues " + issues);
		return repoIO.getIssueMetadata(repoId, issues).thenApply(metadata -> {
			models.insertMetadata(repoId, metadata);
			return metadata;
		}).thenApply(Futures.tap(this::updateUIWithoutMetadata))
		.exceptionally(withResult(new HashMap<>()));
	}

	public Set<String> getOpenRepositories() {
		return models.toModels().stream()
			.map(Model::getRepoId)
			.collect(Collectors.toSet());
	}

	public boolean isAlreadyOpen(String repoId) {
		return getOpenRepositories().contains(repoId);
	}

	public void setDefaultRepo(String repoId) {
		models.setDefaultRepo(repoId);
	}

	public String getDefaultRepo() {
		return models.getDefaultRepo();
	}
}

