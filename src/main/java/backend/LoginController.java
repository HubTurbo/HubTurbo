package backend;

import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.RepositoryId;
import ui.UI;
import util.HTLog;
import util.Utility;

import java.util.concurrent.ExecutionException;

public class LoginController {

    private static final Logger logger = HTLog.get(LoginController.class);
    private Logic logic;

    private String owner = "";
    private String repo = "";
    private String username = "";
    private String password = "";

    // Assumed to be always present when app starts
    public UserCredentials credentials = null;

    protected LoginController(Logic logic) {
        this.logic = logic;
        getPreviousLoginDetails();
    }

    private boolean login(String username, String password) {
        String message = "Logging in as " + username;
        logger.info(message);
        UI.status.displayMessage(message);
        credentials = new UserCredentials(username, password);
        try {
            return logic.repoIOLogin(credentials).get();
        } catch (InterruptedException | ExecutionException e1) {
            HTLog.error(logger, e1);
            return false;
        }
    }

    public void getPreviousLoginDetails() {
        if (logic.prefs.getLastViewedRepository().isPresent()) {
            owner = logic.prefs.getLastViewedRepository().get().getOwner();
            repo = logic.prefs.getLastViewedRepository().get().getName();
        }
        username = logic.prefs.getLastLoginUsername();
        password = logic.prefs.getLastLoginPassword();
    }

    public boolean attemptLogin() {
        if (Utility.isWellFormedRepoId(owner, repo) && !username.isEmpty() && !password.isEmpty()) {
            if (login(username, password)) {
                logic.prefs.setLastLoginCredentials(username, password);
                return true;
            }
        }
        return false;
    }

    public boolean attemptLogin(String owner, String repo, String username, String password) {
        this.owner = owner;
        this.repo = repo;
        this.username = username;
        this.password = password;
        return attemptLogin();
    }

    public String getOwner() {
        return owner;
    }

    public String getRepo() {
        return repo;
    }

    public String getUsername() {
        return username;
    }

    public String getRepoId() {
        return RepositoryId.create(owner, repo).generateId();
    }
}
