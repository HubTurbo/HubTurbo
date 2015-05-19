package backend;

import backend.resource.Model;
import backend.resource.MultiModel;
import org.apache.logging.log4j.Logger;
import util.HTLog;
import util.Utility;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Logic {

	private static final Logger logger = HTLog.get(Logic.class);

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

	public CompletableFuture<Map<Integer, IssueMetadata>> getIssueMetadata(String repoId, List<Integer> issues) {
		return repoIO.getIssueMetadata(repoId, issues).thenApply(metadata -> {
			// TODO process the metadata by inserting it into the model
			return metadata;
		});
	}
}

