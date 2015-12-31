package backend;

import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.RepositoryId;
import ui.UI;
import util.HTLog;
import util.Utility;

import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

public final class LoginController {

    private static final Logger logger = HTLog.get(LoginController.class);
    private final Logic logic;

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
        Optional<RepositoryId> lastViewedRepository = logic.prefs.getLastViewedRepository();
        TreeSet<String> storedRepos = new TreeSet<>(logic.getStoredRepos());
        if (lastViewedRepository.isPresent() &&
                storedRepos.contains(RepositoryId.create(
                        lastViewedRepository.get().getOwner(),
                        lastViewedRepository.get().getName()).generateId())) {
            owner = lastViewedRepository.get().getOwner();
            repo = lastViewedRepository.get().getName();
        } else if (!storedRepos.isEmpty()) {
            RepositoryId repositoryId = RepositoryId.createFromId(storedRepos.first());
            owner = repositoryId.getOwner();
            repo = repositoryId.getName();
        }
        username = logic.prefs.getLastLoginUsername();
        password = logic.prefs.getLastLoginPassword();
    }

    public boolean attemptLogin() {

        boolean validRepoId = Utility.isWellFormedRepoId(owner, repo) && !username.isEmpty() && !password.isEmpty();
        boolean loginSuccessful = login(username, password);

        if (validRepoId && loginSuccessful) {
            logic.prefs.setLastLoginCredentials(username, password);
            return true;
        }
        return false;
    }

    public boolean attemptLogin(String owner, String repo, String username, String password) {
        this.owner = Utility.removeAllWhitespace(owner);
        this.repo = Utility.removeAllWhitespace(repo);
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

    public String getPassword() {
        return password;
    }

    public String getRepoId() {
        return RepositoryId.create(owner, repo).generateId();
    }
}
