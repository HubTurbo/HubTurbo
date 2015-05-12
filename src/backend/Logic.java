package backend;

import javafx.application.Platform;

public class Logic {

	private final MultiModel models = new MultiModel();

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
		repoIO.login(credentials).thenAccept(success -> {
			System.out.println(success);
		});
	}

	public void openRepository(String repoId) {
		repoIO.openRepository(repoId).thenAccept(newModel -> {
			// Thread confinement
			// TODO Would this cause problems? State wouldn't update immediately
			// Maybe a synchronized instance method?
			System.out.println("done getting " + repoId);
			System.out.println(newModel.getIssues());
			Platform.runLater(() -> {
				models.add(newModel);
				System.out.println(newModel);
//				uiManager.update(newModel);
			});
		});
	}
}

