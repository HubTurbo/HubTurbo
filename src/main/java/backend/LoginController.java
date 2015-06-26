package backend;

import org.apache.logging.log4j.Logger;
import ui.UI;
import util.HTLog;

import java.util.concurrent.CompletableFuture;

public class LoginController {

    private static final Logger logger = HTLog.get(LoginController.class);
    private RepoIO repoIO;

    // Assumed to be always present when app starts
    public UserCredentials credentials = null;

    protected LoginController(RepoIO repoIO) {
        this.repoIO = repoIO;
    }

    public CompletableFuture<Boolean> login(String username, String password) {
        String message = "Logging in as " + username;
        logger.info(message);
        UI.status.displayMessage(message);

        credentials = new UserCredentials(username, password);
        return repoIO.login(credentials);
    }

}
