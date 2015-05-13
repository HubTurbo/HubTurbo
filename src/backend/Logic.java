package backend;

import javafx.application.Platform;
import util.Utility;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class Logic {

	private MultiModel models = new MultiModel();

//	private final UIManager uiManager;

	private RepoIO repoIO = new RepoIO();

	// Assumed to be always present when app starts
	private UserCredentials credentials = null;

	public Logic() {
//		UIManager uiManager
//		this.uiManager = uiManager;
	}

	public void login(String username, String password) {
		credentials = new UserCredentials(username, password);
		repoIO.login(credentials).thenAccept(System.out::println);
	}

	public void refresh() {
		Utility.sequence(models.toModels().stream()
			.map(repoIO::updateModel)
			.collect(Collectors.toList())).thenAccept(models::replace);
	}

	public void openRepository(String repoId) {
		repoIO.openRepository(repoId).thenAccept(newModel -> {
			System.out.println("done getting " + repoId);
			System.out.println(newModel.getIssues());
			models.add(newModel);
		});
	}
}

