package prefs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.RepositoryId;
import util.FileHelper;
import util.HTLog;
import util.JsonHelper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents persistent user configuration. Maps to a file on disk.
 *
 * Overrides PMD's recommendation that this class should be final.
 * It cannot be as we need to mock it.
 */
public class Preferences { // NOPMD

    private static final Logger logger = LogManager.getLogger(Preferences.class.getName());

    public static final String DIRECTORY = "settings";

    public static final String SESSION_CONFIG_FILENAME = "global.json";
    public static final String USER_CONFIG_FILENAME = "user.json";

    private final String sessionConfigFileName;
    private final String userConfigFileName;

    private final SessionConfig sessionConfig;
    private final UserConfig userConfig;

    /**
     * Initialises a Preferences instance that contain the configurations in the config files specified
     * @param sessionConfigFileName The file name of the session config file which stores session config values.
     * @param userConfigFileName The file name of the user config file which stores user config values.
     * @return The initialised Preferences instance
     */
    public static Preferences load(String sessionConfigFileName, String userConfigFileName) {
        return new Preferences(sessionConfigFileName, userConfigFileName, false);
    }

    /**
     * Initialises a Preferences instance that contain a clean slate of configuration values in the config files
     * specified
     * @param sessionConfigFileName The file name of the session config file which stores session config values.
     * @param userConfigFileName The file name of the user config file which stores user config values.
     * @return The initialised Preferences instance
     */
    public static Preferences create(String sessionConfigFileName, String userConfigFileName) {
        return new Preferences(sessionConfigFileName, userConfigFileName, true);
    }

    /**
     * @param sessionConfigFileName The name of the session config file
     * @param ignoreExisting True if existing config file is to be ignored.
     */
    private Preferences(String sessionConfigFileName, String userConfigFileName, boolean ignoreExisting) {
        this.sessionConfigFileName = sessionConfigFileName;
        this.userConfigFileName = userConfigFileName;

        this.sessionConfig = loadConfig(sessionConfigFileName, SessionConfig.class, ignoreExisting);
        this.userConfig = loadConfig(userConfigFileName, UserConfig.class, ignoreExisting);
    }

    /**
     * Loads a Config instance from a file on disk
     * @param configFileName The filename of the config file on disk
     * @param configClass The class of the config to load
     * @param ignoreExisting True if ignore the config file already
     * @return The loaded Config instance
     */
    private <T extends Config> T loadConfig(String configFileName, Class<T> configClass, boolean ignoreExisting) {
        FileHelper configFileHelper = new FileHelper(DIRECTORY, configFileName);
        String fileContents;
        if (ignoreExisting) {
            fileContents = configFileHelper.loadNewFile();
        } else {
            try {
                fileContents = configFileHelper.loadFileContents();
            } catch (IOException e) {
                // if we can't read the file, just ignore the existing file
                fileContents = configFileHelper.loadNewFile();
            }
        }

        try {
            return new JsonHelper().createConfigFromJson(fileContents, configClass);

        } catch (IllegalAccessException | InstantiationException e) {
            HTLog.error(logger, e);
            assert false;
            // If we cannot instantiate, it is a programmer error.
            // Since we have to return something even after assert false, we return a null config.
            return null;
        }
    }

    /**
     * Saves the session and user configs to file
     */
    public void save() {
        try {
            saveConfig(sessionConfig, sessionConfigFileName);

        } catch (IOException e) {
            HTLog.error(logger, e);
            logger.error("Could not save session config");
        }

        try {
            saveConfig(userConfig, userConfigFileName);

        } catch (IOException e) {
            HTLog.error(logger, e);
            logger.error("Could not save user config");
        }
    }

    /**
     * Saves a Config to file
     * @param config The config object to save
     * @param configFileName The file name of the config file on disk
     */
    private void saveConfig(Config config, String configFileName) throws IOException {
        JsonHelper jsonHelper = new JsonHelper();
        String jsonString = jsonHelper.createJsonFromConfig(config);
        FileHelper fileHelper = new FileHelper(DIRECTORY, configFileName);
        fileHelper.writeFileContents(jsonString);
    }

    // Last login credentials. While the main UI is running (i.e. logged in successfully), last login
    // credentials are guaranteed to be the current user's credentials thanks to setLastLoginCredentials
    // being called immediately after a successful login in LoginDialog.
    public String getLastLoginPassword() {
        return sessionConfig.getLastLoginPassword();
    }

    /**
     * Retrieves the last login username
     */
    public String getLastLoginUsername() {
        return sessionConfig.getLastLoginUsername();
    }

    /**
     * Sets the last login credentials
     * @param username The username used during the last login
     * @param password The password used during the last login
     */
    public void setLastLoginCredentials(String username, String password) {
        sessionConfig.setLastLoginCredentials(username, password);
    }

    /**
     * Retrieves a list of the last open filters
     */
    public List<String> getLastOpenFilters() {
        return sessionConfig.getLastOpenFilters();
    }

    /**
     * Retrieves a list of panel names
     */
    public List<String> getPanelNames() {
        return sessionConfig.getPanelNames();
    }

    /**
     * Retrieves a list of panel infos
     */
    public List<PanelInfo> getPanelInfo() {
        return sessionConfig.getPanelInfo();
    }

    /**
     * Sets the panel infos
     * @param panelInfo The list of panel infos to set to
     */
    public void setPanelInfo(List<PanelInfo> panelInfo) {
        sessionConfig.setPanelInfo(panelInfo);
    }

    /**
     * Interface to configuration files
     */

    /**
     * Boards
     */

    public void addBoard(String name, List<PanelInfo> panels) {
        assert name != null && panels != null;
        sessionConfig.addBoard(name, panels);
    }

    /**
     * Retrieves a map of all the boards
     */
    public Map<String, List<PanelInfo>> getAllBoards() {
        return sessionConfig.getAllBoards();
    }

    /**
     * Retrieves a list of all the board names
     */
    public List<String> getAllBoardNames() {
        return new ArrayList<>(getAllBoards().keySet());
    }

    /**
     * Removes the board specified
     * @param name The name of the board to remove
     */
    public void removeBoard(String name) {
        sessionConfig.removeBoard(name);
    }

    /**
     * Sets the last open board
     * @param board The name of the last open board.
     */
    public void setLastOpenBoard(String board) {
        sessionConfig.setLastOpenBoard(board);
    }

    /**
     * Retrieves the name of the last open board.
     * @return An Optional of a nullable board name
     */
    public Optional<String> getLastOpenBoard() {
        return sessionConfig.getLastOpenBoard();
    }

    /**
     * Switches the board to the next one. Cycles through the boards one at a time.
     * @return The new board selected
     */
    public Optional<String> switchBoard() {
        if (getLastOpenBoard().isPresent() && getAllBoards().size() > 1) {
            List<String> boardNames = getAllBoardNames();
            int lastBoard = boardNames.indexOf(getLastOpenBoard().get());
            int index = (lastBoard + 1) % boardNames.size();
            
            setLastOpenBoard(boardNames.get(index));
        }
        
        return getLastOpenBoard();
    }

    /**
     * Clears the last open board
     */
    public void clearLastOpenBoard() {
        sessionConfig.clearLastOpenBoard();
    }

    /**
     * Retrieves the panels of a board.
     * @param boardName The name of the board to retrieve the panels from
     * @return A list of panel infos from that board
     */
    public List<PanelInfo> getBoardPanels(String boardName) {
        return sessionConfig.getBoardPanels(boardName);
    }

    /**
     * Clears all boards.
     */
    public void clearAllBoards() {
        sessionConfig.clearAllBoards();
    }

    /**
     * Session configuration
     */

    /**
     * Sets the last viewed repo to the specified repo
     * @param repository The last viewed repo
     */
    public void setLastViewedRepository(String repository) {
        sessionConfig.setLastViewedRepository(repository);
    }

    /**
     * Retrieves the last viewed repo.
     * @return An Optional of the last viewed repo.
     */
    public Optional<RepositoryId> getLastViewedRepository() {
        if (sessionConfig.getLastViewedRepository().isEmpty()) {
            return Optional.empty();
        } else {
            RepositoryId repositoryId = RepositoryId.createFromId(sessionConfig.getLastViewedRepository());
            if (repositoryId == null) {
                return Optional.empty();
            } else {
                return Optional.of(repositoryId);
            }
        }
    }

    /**
     * Clears marked read at of an issue at the specified repo
     * @param repoId The repo that this issue resides in
     * @param issue The issue to clear
     */
    public void clearMarkedReadAt(String repoId, int issue) {
        sessionConfig.clearMarkedReadAt(repoId, issue);
    }

    /**
     * Sets the marked read at for an issue in a repo, to a certain time
     * @param repoId The repo of the issue
     * @param issue The issue to set
     * @param time The time it was marked read
     */
    public void setMarkedReadAt(String repoId, int issue, LocalDateTime time) {
        sessionConfig.setMarkedReadAt(repoId, issue, time);
    }

    /**
     * Retrieves the marked read at for a specified issue in a repo
     * @param repoId The repo of the issue
     * @param issue The issue to retrieve the marked read at
     * @return An Optional of the marked read at
     */
    public Optional<LocalDateTime> getMarkedReadAt(String repoId, int issue) {
        return sessionConfig.getMarkedReadAt(repoId, issue);
    }

    /**
     * Retrieves the map of keyboard shortcuts
     */
    public Map<String, String> getKeyboardShortcuts() {
        return sessionConfig.getKeyboardShortcuts();
    }

    /**
     * Sets the keyboard shortcuts to the specified mapping
     * @param keyboardShortcuts The mapping of keyboard shortcuts to set to.
     */
    public void setKeyboardShortcuts(Map<String, String> keyboardShortcuts) {
        sessionConfig.setKeyboardShortcuts(keyboardShortcuts);
    }
}
