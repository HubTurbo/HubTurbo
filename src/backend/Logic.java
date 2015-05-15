package backend;

import backend.resource.Model;
import backend.resource.MultiModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.Utility;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Logic {

	private static final Logger logger = LogManager.getLogger(Logic.class.getName());

	private MultiModel models = new MultiModel();

//	private final UIManager uiManager;

	private RepoIO repoIO = new RepoIO();

	// Assumed to be always present when app starts
	private UserCredentials credentials = null;

	public Logic() {
//		UIManager uiManager
//		this.uiManager = uiManager;
	}

	public CompletableFuture<Boolean> login(String username, String password) {
		logger.info("Logging in as " + username);
		credentials = new UserCredentials(username, password);
		return repoIO.login(credentials);
	}

	public void refresh() {
		logger.info("Refreshing " + models.toModels().stream()
			.map(Model::getRepoId).collect(Collectors.toList()));
		Utility.sequence(models.toModels().stream()
			.map(repoIO::updateModel)
			.collect(Collectors.toList())).thenAccept(models::replace);
	}

	public CompletableFuture<Void> openRepository(String repoId) {
		logger.info("Opening " + repoId);
		return repoIO.openRepository(repoId).thenAccept(models::add);
	}
}

